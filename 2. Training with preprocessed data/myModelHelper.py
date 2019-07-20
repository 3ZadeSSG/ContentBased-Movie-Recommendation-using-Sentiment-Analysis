import numpy as np
import os
from string import punctuation
from collections import Counter
import torch
from torch.utils.data import TensorDataset,DataLoader
import torch.nn as nn

def createData(positiveDataPath,negativeDataPath):
    reviews=[]
    labels=[]
    all_files_neg = os.listdir(negativeDataPath)
    all_files_pos = os.listdir(positiveDataPath)
    for file_location in all_files_pos:
        with open(positiveDataPath+file_location,'r',encoding="utf8")as f:
            reviews.append(f.read())
            labels.append("positive")
            
    for file_location in all_files_neg:
        with open(negativeDataPath+file_location,'r',encoding="utf8")as f:
            reviews.append(f.read())
            labels.append("negative")
    myDict={}
    for i in range(len(reviews)):
       myDict[i]=i
    reviewArray=np.arange(len(reviews))
    np.random.shuffle(reviewArray)
    reviewString=""
    labelString=""
    for i in list(reviewArray):
        reviewString=reviewString+reviews[i]+"\n"
        labelString=labelString+labels[myDict[i]]+"\n"
 
    return reviewString,labelString
	

def featuresPadding(reviews,sequenceLength):
  features=np.zeros((len(reviews),sequenceLength),dtype=int)
  for i,row in enumerate(reviews):
    features[i,-len(row):]=np.array(row)[:sequenceLength]
  return features


def getFeaturesLabels(reviews,labels,seq_length):
  reviews=reviews.lower()
  all_text=''.join([c for c in reviews if c not in punctuation])
  reviews_split=all_text.split('\n')
  all_text=' '.join(reviews_split)
  words=all_text.split()
  counts=Counter(words)
  vocab=sorted(counts,key=counts.get,reverse=True)
  vocab_to_int={word:ii for ii, word in enumerate(vocab,1)}
  reviews_ints=[]
  for review in reviews_split:
    reviews_ints.append([vocab_to_int[word] for word in review.split()])
  labels_split=labels.split('\n')
  encoded_labels=np.array([1 if label=='positive' else 0 for label in labels_split])
  reviews_lens=Counter([len(x) for x in reviews_ints])
  non_zero_idx=[ii for ii,review in enumerate(reviews_ints) if len(review)!=0]
  reviews_ints=[reviews_ints[ii] for ii in non_zero_idx]
  encoded_labels=np.array([encoded_labels[ii] for ii in non_zero_idx])
  features=featuresPadding(reviews_ints,seq_length)
  return features,encoded_labels,vocab_to_int


def createTrainTestValidateData(split_frac,features,encoded_labels):
  split_idx = int(len(features)*split_frac)
  train_x, remaining_x = features[:split_idx], features[split_idx:]
  train_y, remaining_y = encoded_labels[:split_idx], encoded_labels[split_idx:]
  test_idx = int(len(remaining_x)*0.5)
  val_x, test_x = remaining_x[:test_idx], remaining_x[test_idx:]
  val_y, test_y = remaining_y[:test_idx], remaining_y[test_idx:]
  return train_x,train_y,test_x,test_y,val_x,val_y


