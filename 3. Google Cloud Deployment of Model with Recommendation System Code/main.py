from flask import Flask,request
from flask_restful import Resource,Api
from commons import *
import sys
import logging
import os
from TwitterAnalyzer import *
from movieRecommendorOnUserData import *

app=Flask(__name__)
api=Api(app)

##############################################################################
'''
Input : Query string to be searched on twitter and analyze the percentage of Positive tweets about that topic
Output: Percentage value of positive tweets
Uses: 1. Trainded model to analyzer each tweet's sentiment.
	  2. Vocabulary to integer mapping dictonary to encode tweets.
	  3. Twitter API key to retrive tweets
'''
class Analyze(Resource):
	def get(self,query):
		result=query.split('+')
		search=' '.join(result)
		final_result=getTwitterSentiment(search)
		del search
		del result
		return {'result ':final_result}

##############################################################################
'''
Input: A text whose sentiment result is about to be found
Output: Sentiment Score(Between 0-1) with Positive/Negative Label
Uses: 1. Trained Model for prediction.
	  2. Vocabulary to Integer Mapping dictonary to encode input.
'''
class Multi(Resource):
	def get(self,num):
		result=num.split('+')
		review=' '.join(result)
		predicted_result=getSentimentPredictionResult(review)
		del review
		del result
		return {'result ':predicted_result}

##############################################################################
'''
Input:	UserID and Movie Title.
Output: Returns successs message after adding the movie to user likes. If user is new, then new data entry is created for user
Uses: 1. User's Like and Dislike data mapped with UserID
'''

class AddLike(Resource):
	def get(self,likedMovie):
		#print("\n================\nPath Status")
		#print(os.path.isdir("/home"))

		result=likedMovie.split('+')
		userID=result[0]
		result=result[1:]
		movieTitle=' '.join(result)
		final_result=addNewLike(userID,movieTitle)
		return {'result ':final_result}

##############################################################################
'''
Input:	UserID and Movie Title.(Format: UserID+MovieTitle, Example: user123+Toy+Story)
Output: Returns successs message after adding the movie to user dislikes. If user is new, then new data entry is created for user
Uses: 1. User's Like and Dislike data mapped with UserID
'''
class AddDislike(Resource):
	def get(self,dislikedMovie):
		result=dislikedMovie.split('+')
		userID=result[0]
		result=result[1:]
		movieTitle=' '.join(result)
		final_result=addNewDislike(userID,movieTitle)
		return {'result ':final_result}

##############################################################################
'''
Input: UserID
Output: JSON response of recommended movies to the user, each entry containing
			1. Movie Title
			2. Poster URL 
			3. Overview
			4. Release Date
Uses: Content based recommendation method
'''
class GetRecommendation(Resource):
	def get(self,userID):
		final_result=getRecommendedMoviesID(userID)
		return final_result


# Bind each class to corresponding calling strings
api.add_resource(Multi,'/sentiment/<num>')
api.add_resource(Analyze,'/analyze/<query>')
api.add_resource(AddLike,'/addLike/<likedMovie>')
api.add_resource(AddDislike,'/addDislike/<dislikedMovie>')
api.add_resource(GetRecommendation,'/getRecommendation/<userID>')


if __name__=='__main__':
	app.run(port=os.getenv('PORT',5000))
	app.logger.addHandler(logging.StreamHandler(sys.stdout))
	app.logger.setLevel(logging.ERROR)
