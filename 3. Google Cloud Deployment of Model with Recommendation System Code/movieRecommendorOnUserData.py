import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel
import urllib.request
import json
import os
import psutil

# Location where user data have been saved
userLikeDataLocation='/home/userLikeData.npy'
userDislikeDataLocation='/home/userDislikeData.npy'
omdbAPIKEY="" # Your OMDB API KEY goes here

# if user data is not created then create one dictionary file where all users will be saved
def createFiles():
    like={}
    dislike={}
    saveUserData(userLikeDataLocation,like)
    saveUserData(userDislikeDataLocation,dislike)
    print("\n\n===============Creating Files===============\n\n")

# get the index of movie by its title 
def getTitleIndex(title,tempDF):
    try:
        return tempDF[tempDF['title'].str.contains( title+".*",regex=True,na=False,case=False)]['movieId'].tolist()[0]
    except:
        return -1


#get rating of movie from data.tsv dataset by searching against the imdb id
def getRating(imdb_id,ratings):
    try:
        return ratings[ratings['tconst']==imdb_id]['averageRating'].tolist()[0]
    except:
        return -1
    

'''
Input: User ID
Output: 1. List of recommended movies
        2. In case user hasn't liked any movies the return "user is not in database"
        3. If movie is not in the dataset then reutrn "movie with title cannot be found"
'''
def getRecommendedMoviesID(userID):

    #read the ratings file
    ratings=pd.read_csv('data.tsv', sep='\t', header=0) 
    ratings=ratings[['tconst','averageRating']]
    
    #if user databse is not created then create one
    if(not os.path.isfile(userLikeDataLocation)):
        createFiles()

    print("\n=========Status: Load user data")


    # load the user data and serach for userID, if not in database then return error
    userData=loadUserData(userLikeDataLocation)
    if not userID in userData.keys():
        return {"status":"False",
                "result":"User is not in database"}
    else:

        tempList=userData[userID]
        title=""
        if(len(tempList)>0):
            title=tempList[-1] #get the last movie user has liked
        else:
            #if user hasn't liked any movie then return user hasn't liked any movie yet
            return {"status":"False",
                "result":"User hasn't liked any movie so far"}
        
        #load the dislike data to remove movies from recommendation which user will not like
        userData=loadUserData(userDislikeDataLocation)
        dislikeList=[]
        if userID in userData.keys():
            dislikeList=userData[userID]
        
        
        
        MoviesID=[]
        data_location="movies.csv"
        movies=pd.read_csv(data_location)
        print("\n=========Status: Reding movies")

        # for each movie create tfidf vector over categories
        tf=TfidfVectorizer(analyzer="word",ngram_range=(1,2),min_df=0,stop_words="english")
        print(psutil.virtual_memory())
        print("\n=========Status: Creating TfIdfMatrix")
        tfidf_matrix=tf.fit_transform(movies['genres'])
        print(psutil.virtual_memory())
        print("\n=========Status: Created TfIdfMatrix")
        print(psutil.virtual_memory())
        
        #get the cosine similarity between each movie from multidimensional vector space
        cosine_sim=linear_kernel(tfidf_matrix,tfidf_matrix)
        print(psutil.virtual_memory())
        print("\n=========Status: Creted CosinefMatrix")


        titles=movies['title']
        indices=pd.Series(movies.index,index=movies['title'])

        tempDF=movies[['movieId','title']].iloc[:,:]

        idx=getTitleIndex(title,tempDF)
        if(idx==-1):
            return {"status":"False",
                    "result":"No movie with the "+title+" title in database"}
        else:
            idx=idx-1
            sim_scores=list(enumerate(cosine_sim[idx]))
            sim_scores=sorted(sim_scores,key=lambda x: x[1],reverse=True)
            sim_scores=sim_scores[1:21]
            movie_indices=[i[0] for i in sim_scores]
            RecommendationResult=titles.iloc[movie_indices]

            linkDF=pd.read_csv('links.csv')

            #from most similar movies select top 15 to be used with rating filter
            for element in RecommendationResult[:15]:
                searchID=movies[movies['title']==element]['movieId'].tolist()[0]
                imdbIndex=linkDF[linkDF['movieId']==searchID]['imdbId'].tolist()[0]
                imdbIndex=str(imdbIndex)
                while(not len(imdbIndex)==8):
                    imdbIndex='0'+imdbIndex

                MoviesID.append('tt'+imdbIndex)

            ######################################
            #get imdbId of each movie selected
            imdbIndex=linkDF[linkDF['movieId']==idx+1]['imdbId'].tolist()[0]
            imdbIndex=str(imdbIndex)
            while(not len(imdbIndex)==8):
                imdbIndex='0'+imdbIndex
            TitleIndexID='tt'+imdbIndex

            titleIndexScore=getRating(TitleIndexID,ratings)
            finalResult=[]
            for element in MoviesID:
                score=getRating(element,ratings)

                #fusion formula to get final scores
                score=10-abs(score-titleIndexScore)
                finalResult.append((element,score))

            #sort movies in descending order of score
            contents=sorted(finalResult, key=lambda x: x[1],reverse=True)

            #take only top 10 movies
            contents=contents[:10]
            resultDict=[]
            

            #create a JSON file for each movie containing its Title, Release data, Overview, Genre, Poster to be rutrned as result
            for url,_ in contents:
                requestURL="https://www.omdbapi.com/?apikey="+omdbAPIKEY+"&i="+url
                element=urllib.request.urlopen(requestURL).read()
                root=json.loads(element)
                if not root['Title'] in dislikeList:
                    movieDict={
                                "title":root['Title'],
                                "release":root['Released'],
                                "overview":root['Plot'],
                                "genre":root['Genre'],
                                "poster":root['Poster'],        
                            }
                    resultDict.append(movieDict)

                responseJSON={"status":"True",
                        "result":resultDict} 

            
        return responseJSON
    
    

# Load the user data into the dictionary
def loadUserData(fileLocation):
    return np.load(fileLocation,allow_pickle=True).item()


#Save user data into file
def saveUserData(fileLocation,data):
    np.save(fileLocation,data)



'''
Input: userID, Movie Title
Output: Add movie to user Likes list and remove it from dislike list if exists
'''
def addNewLike(userID,movieTitle):
    if(not os.path.isfile(userLikeDataLocation)):
        createFiles()

    userData=loadUserData(userLikeDataLocation)
    if userID in userData.keys():
        tempList=userData[userID]
        if not movieTitle in tempList:
            tempList.append(movieTitle)
            if(len(tempList)>5):
                tempList=tempList[1:]
        userData[userID]=tempList
        
        try:
            dislikeData=loadUserData(userDislikeDataLocation)
            dislikeData[userID].remove(movieTitle)
            saveUserData(userDislikeDataLocation,dislikeData)
        except:
            del dislikeData
            
    else:
        userData[userID]=[movieTitle]
    
    saveUserData(userLikeDataLocation,userData)
    return True


'''
Input: userID, Movie Title
Output: Add movie to user Dislikes list and remove it from like list if exists
'''
def addNewDislike(userID,movieTitle):
    if(not os.path.isfile(userLikeDataLocation)):
        createFiles()
        
    userData=loadUserData(userDislikeDataLocation)
    if userID in userData.keys():
        tempList=userData[userID]
        if not movieTitle in tempList:
            tempList.append(movieTitle)
        userData[userID]=tempList
        
        try:
            likeData=loadUserData(userLikeDataLocation)
            likeData[userID].remove(movieTitle)
            saveUserData(userLikeDataLocation,likeData)
        except:
            del likeData
            
    else:
        userData[userID]=[movieTitle]
    
    saveUserData(userDislikeDataLocation,userData)
    return True
