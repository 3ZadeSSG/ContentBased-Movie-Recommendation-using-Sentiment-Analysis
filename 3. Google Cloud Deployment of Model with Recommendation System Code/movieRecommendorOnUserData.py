import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel
import urllib.request
import json
import os
import psutil

userLikeDataLocation='/home/userLikeData.npy'
userDislikeDataLocation='/home/userDislikeData.npy'

def createFiles():
    like={}
    dislike={}
    saveUserData(userLikeDataLocation,like)
    saveUserData(userDislikeDataLocation,dislike)
    print("\n\n===============Creating Files===============\n\n")

def getTitleIndex(title,tempDF):
    try:
        return tempDF[tempDF['title'].str.contains( title+".*",regex=True,na=False,case=False)]['movieId'].tolist()[0]
    except:
        return -1

def getRating(imdb_id,ratings):
    try:
        return ratings[ratings['tconst']==imdb_id]['averageRating'].tolist()[0]
    except:
        return -1
    
def getRecommendedMoviesID(userID):
    ratings=pd.read_csv('data.tsv', sep='\t', header=0)
    ratings=ratings[['tconst','averageRating']]
    
    if(not os.path.isfile(userLikeDataLocation)):
        createFiles()

    print("\n=========Status: Load user data")

    userData=loadUserData(userLikeDataLocation)
    if not userID in userData.keys():
        return {"status":"False",
                "result":"User is not in database"}
    else:
        tempList=userData[userID]
        title=""
        if(len(tempList)>0):
            title=tempList[-1]
        else:
            return {"status":"False",
                "result":"User hasn't liked any movie so far"}
        
        userData=loadUserData(userDislikeDataLocation)
        dislikeList=[]
        if userID in userData.keys():
            dislikeList=userData[userID]
        
        
        
        MoviesID=[]
        data_location="movies.csv"
        movies=pd.read_csv(data_location)
        print("\n=========Status: Reding movies")

        tf=TfidfVectorizer(analyzer="word",ngram_range=(1,2),min_df=0,stop_words="english")
        print(psutil.virtual_memory())
        print("\n=========Status: Creating TfIdfMatrix")
        tfidf_matrix=tf.fit_transform(movies['genres'])
        print(psutil.virtual_memory())
        print("\n=========Status: Created TfIdfMatrix")
        print(psutil.virtual_memory())
        
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

            for element in RecommendationResult[:15]:
                searchID=movies[movies['title']==element]['movieId'].tolist()[0]
                imdbIndex=linkDF[linkDF['movieId']==searchID]['imdbId'].tolist()[0]
                imdbIndex=str(imdbIndex)
                while(not len(imdbIndex)==8):
                    imdbIndex='0'+imdbIndex

                MoviesID.append('tt'+imdbIndex)

            ######################################
            imdbIndex=linkDF[linkDF['movieId']==idx+1]['imdbId'].tolist()[0]
            imdbIndex=str(imdbIndex)
            while(not len(imdbIndex)==8):
                imdbIndex='0'+imdbIndex
            TitleIndexID='tt'+imdbIndex

            titleIndexScore=getRating(TitleIndexID,ratings)
            finalResult=[]
            for element in MoviesID:
                score=getRating(element,ratings)
                score=10-abs(score-titleIndexScore)
                finalResult.append((element,score))

            contents=sorted(finalResult, key=lambda x: x[1],reverse=True)
            contents=contents[:10]
            resultDict=[]
            
            for url,_ in contents:
                requestURL="https://www.omdbapi.com/?apikey=815fe2a8&i="+url
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
    
    

def loadUserData(fileLocation):
    return np.load(fileLocation,allow_pickle=True).item()

def saveUserData(fileLocation,data):
    np.save(fileLocation,data)


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

