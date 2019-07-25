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
              
   
              

# Proposed Model
Creating the rating database is possible on production environment, but the twitter analyzer model have been implemented to be used there. The purposed model has been shown in following figure.

   _**1. Based on user likes, generate list of recommended movies using traditional content based filtering method.**_
   
   _**2. For each movie in database, the rating for that movie needs to be generated.**_ 

   _**3. For each movie, analyze the tweets by passing it to sentiment prediction model and generate the percentage of positive results as rating.**_
            
   _**4. For each recommended movie, use it’s rating score  to fuse it with rating of movies liked by user.**_ 
    
   _**5. Movies with highest score will be considered for recommendation.**_


<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Recommendation Modelpng.png">

# Final System
<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Compute Engine.png">

# Google Cloud App Engine Deployment

Client can use the API methods to call the server to get respone, the API methods are defined in Folder __3. Google Cloud Deployment__ inside __main.py__ file. To deploy the model, paste your sentiment analysis checkpoint file into the Folder __3.Google Cloud Deployment__ and run the following command form termial:
  
    > gcloud app deploy

An example of output when deploying the app in my case:
          
          F:\MajorProject\3. Google Cloud Deployment of Model with Recommendation System Code>gcloud app deploy
    Services to deploy:

    descriptor:      [F:\MajorProject\3. Google Cloud Deployment of Model with Recommendation System Code\app.yaml]
    source:          [F:\MajorProject\3. Google Cloud Deployment of Model with Recommendation System Code]
    target project:  [major-project-final-246818]
    target service:  [default]
    target version:  [20190724t221233]
    target url:      [https://major-project-final-246818.appspot.com]


    Do you want to continue (Y/n)?
    
 After your own app has been deployed you need to replace the API Urls from Android Java files with your own API urls. Which means __"https://major-project-final-246818.appspot.com"__ with your own generated url.
 
 

# Video Walkthrough
User Logs In and likes a movie which gets saved in database|User gets recommended movie on recommendations page| Twitter Analyzer to demonstrate that it can be used to create the rating database
:-------------------------:|:-------------------------:|:-------------------------:
<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Login.gif" width="250">|<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Recommendation.gif" width="250">|<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Twitter Analyzer.gif" width="250">


# Additional Screenshots
Liking a movie|Disliking a movie| Navigation Drawer
:-------------------------:|:-------------------------:|:-------------------------:
<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Screenshot Like.png" width="250">|<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Screenshot Dislike.png" width="250">|<img src= "https://raw.githubusercontent.com/3ZadeSSG/ContentBased-Movie-Recommendation-using-Sentiment-Analysis/master/5. Screenshots/Screenshot Nav Window.png" width="250">



# Upgrades

  * Calculating cosine similarity matrix each time is expensive, it can be done once, and each row can be saved individually so that it can be reloaded by movie index.
  
  * Twitter Analyzer will give more accurate result if number of tweets to be analyzed is more, since data clearning and dropping duplicates will eventually reduce the number of analyzed tweets from 300(selected window size for this system) to 50-150
  
  * As user base grows system can migrate to a Hybrid Recommendation+Sentiment Analysis System as proposed in this research paper (https://arxiv.org/pdf/1811.10804.pdf)
  
  * To avoid loading same posters each time user gets recommendation on movie, the poster thumbnails can be saved locally.

