'''All necessary imports
 'commons' is imported to use the getSentimentPredictionResult function
 All print statements are not required, but are given so that user can see the log messages
 using 'gcloud app logs tail' if deployed online, else can run locally and see the result.
'''
import tweepy
import pandas as pd
import csv
from commons import *
import time
import re
import os

###########################################################################

def readDataFromTwitter(SearchString):
    #To login you need Goto twitter developer console and generate your own keys
    consumer_key = 
    consumer_secret = 
    access_token = 
    access_token_secret = 


    #Perform authentication with Twitter
    auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
    auth.set_access_token(access_token, access_token_secret)
    api = tweepy.API(auth,wait_on_rate_limit=True)
    reviews=[]
    print("\n\tStatus: Logged in successfully.")
    
    #Search the string to get 300 twitter results
    results=tweepy.Cursor(api.search,q=SearchString,tweet_mode='extended').items(300)
    print("\n\tStatus: Reading data.")
    for review in results:
        reviews.append(review.full_text)
    print("\n\tStatus: Reading data complete.")    

    #store the indexes and convert the generated list to a dataframe
    indexes=list(range(len(reviews)))
    df=pd.DataFrame({'Index': indexes,'Tweet': reviews})
    
    print("\n\tStatus: Saving original data.")
    
    print("\n\tStatus: Original data saved successfully.")
    #return dataframe
    return df

###########################################################################
'''
Funciton to clean the tweets by removing:
1. Emojis
2. URLs
3. Additional Padding
4. Tweets with RT only
'''
def cleanTweet(text):
    result=re.sub(r'@[A-Za-z0-9]+','',text)
    result=re.sub('https?://[A-Za-z0-9./]+','',result)
    result.replace(u"\ufffd", "?")
    result=re.sub("[^a-zA-Z]", " ",result)
    result=re.sub("RT", "",result)
    return result

###########################################################################

'''
Function to clean whole dataframe and return only those which will be fed to model
It removes:
1. Duplicate Tweets
2. Tweets with length less than 50 character after clearning with Regular Expression
'''
def cleanData(df):
    print("\n\tStatus: Reading and cleaning unwanted data.")

    dropIndex=[]
    dfNew=pd.DataFrame(columns=['Index','Tweet'])

    for i in range(len(df['Tweet'])):
        result=cleanTweet(df['Tweet'][i])

        if(result==""):
            dropIndex.append(i)
        elif(len(result)<50):
            dropIndex.append(i)
        else:
            dfNew=dfNew.append({'Tweet':result},ignore_index=True)


    print("\n\tStatus: Total dropped data: {}".format(len(dropIndex)))
    print("\n\tStatus: Data cleaned successfully.")
    tempSize=len(dfNew)

    #Drop all duplicate data from dataframe
    dfNew=dfNew.drop_duplicates(subset=['Tweet'], keep='first', inplace=False)
    #dfNew.to_csv('Cleaned'+fileName)
    print("\n\tStatus: No of dropped duplicates :{}".format(tempSize-len(dfNew)))

    #return dataframe with unique tweets
    return dfNew

###########################################################################

def getTwitterSentiment(SearchString):
    #start the time count
    start_time=time.time()
    fileName=readDataFromTwitter(SearchString)

    #call to clean tweets
    cleanedDataFrame=cleanData(fileName)
    
    # Load the model and vocabularyToInt mapping dictonary
    print("\n\tStatus: Loading vocab.")
    vocab_to_int=createData()
    print("\n\tStatus: Loading model.")
    model=load_checkpoint('checkpoint.pth',vocab_to_int)
    
    # Set sequence length to same as the one used in training
    seq_length=200
    print("\n\tStatus: Model loaded successfully.")
    
    print("\n\tStatus: Initiating model and feeding inputs\n\tPlease wait it might take a while.")
    sum=0.0
    index=0
    invalid=0
    PositiveList=[]
    NegativeList=[]
    TotalList=[]

    #For Each tweet pass it to model and place the score into Positive/Negative to get percentage
    for text in cleanedDataFrame['Tweet']:
        result=predict(model,text,vocab_to_int,seq_length)
        if(result=='0'):
            print("One invalid data entry found...ignoring that from calculation!")
            invalid+=1
        else:
        	if(float(result.split(" ")[0]))<0.5:
        		NegativeList.append(float(result.split(" ")[0]))
        	else:
        		PositiveList.append(float(result.split(" ")[0]))

        	sum+=float(result.split(" ")[0])

        if(index%10==0):
            print("\n\t\tProgress: {}/{}".format(index,len(cleanedDataFrame)))
        index+=1

    print("\n\t\tProgress: {}/{}".format(index,len(cleanedDataFrame)))
    print("\n\tStatus: Result returned successfully.")
    print("\n\tTotal processing time: {} minutes".format((time.time()-start_time)/60))
    print("\n\tTotal Number of Processed Responses: {}".format(len(PositiveList)+len(NegativeList)))
    print("\n\tTotal Number of Positive Responses: {}".format(len(PositiveList)))
    print("\n\tTotal Number of Negative Responses: {}".format(len(NegativeList)))
    print("\n\tAverage: {}".format(sum/(len(PositiveList)+len(NegativeList))))

    #Return percentage of positive tweets
    return ((len(PositiveList))*100)/(len(PositiveList)+len(NegativeList))
          
###########################################################################
            
