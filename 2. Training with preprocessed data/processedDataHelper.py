import numpy as np

def saveNumpyArray(myArray,filename):
    np.save(filename,myArray)
    print('Saved successfully!')

def loadNumpyArray(filename):
    return np.load(filename)


def loadDict(filename):
    return np.load(filename,allow_pickle=True).item()

