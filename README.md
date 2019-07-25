# ContentBased-Movie-Recommendation-using-Sentiment-Analysis
App to recommend movies beased on user likes, show twitter sentiment based on search string, show sentiment prediction of a sentence

# Quick Setup Guide

   * The Sentiment Analysis Model checkpoint file is large and cannont be loaded to this repo. To generate your own trained model, run the notebook of Folder: __2. Training with Preprocessed Data__ since the data preprocessing takes longer, hence preprocessed data have been saved there. To utilize the GPU you need to load the notebook on Google Colab.

  * To create the rating database form tweets, you need to create your own Twitter API Key, and then initialize the following variables in TwitterAnalyzer.py placed in __"3. Google Cloud Deployment folder"__
  
                consumer_key = 
                consumer_secret = 
                access_token = 
                access_token_secret = 

  Then calling __*getTwitterSentiment(SearchString)"__ will return the percentage of positive tweets about the search string.
  
  * To load the details about movie and posters you need OMDB API Key and initialize the variables of following files
  
              omdbAPIKEY="" //movieRecommendorOnUserData.py (Folder 3)
              String APIKEY = ""; //MovieRatingSearchFragment.java (Folder 4)
     
   
   * To load the trending page of Explore Fragment in Android app, you need __themoviedb__ API Key
              
              String APIKEY=""; //ExploreFragment.java (Folder 4)
              
   
              

# Purposed Model
Creating the rating database is possible on production environment, but the twitter analyzer model have been implemented to be used there. The purposed model has been shown in following figure.

   _**1. Based on user likes, generate list of recommended movies using traditional content based filtering method.**_
   
   _**2. For each movie in database, the rating for that movie needs to be generated.**_ 

   _**3. For each movie, analyze the tweets by passing it to sentiment prediction model and generate the percentage of positive results as rating.**_
            
   _**4. For each recommended movie, use it’s rating score  to fuse it with rating of movies liked by user.**_ 
    
   _**5. Movies with highest score will be considered for recommendation.**_


<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Recommendation Modelpng.png">

# Final System
<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Compute Engine.png">

# Video Walkthrough
User Logs in and Like a Movie which gets saved in user like database|User gets recommended movieon recommendation page| Twitter Analyzer to demonstrate that it can be used to create the rating database
:-------------------------:|:-------------------------:|:-------------------------:
<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Login.gif" width="250">|<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Recommendation.gif" width="250">|<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Twitter Analyzer.gif" width="250">

