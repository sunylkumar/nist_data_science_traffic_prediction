import pandas as pd
from datetime import timedelta
import numpy as np
import time
import datetime
import math
from multiprocessing import Pool
import warnings
warnings.filterwarnings("ignore")
import pickle

start_time = time.time()


trials = pd.read_csv('prediction_trials.tsv', header=0, sep='\t')
eventsDF = pd.read_csv("events_train.tsv", sep="\t",header=0)

tlist = []
for i in range(len(trials)):
    tlist.append(trials.loc[i])
	
events = eventsDF[['closed_tstamp','event_type','latitude','longitude']]
events["year"] = events["closed_tstamp"].str[:4]

eventTypes = ['accidentsAndIncidents','roadwork', 'precipitation', 'deviceStatus', 'obstruction', 'trafficConditions']

mypoint = events[[ 'event_type', 'year', 'longitude', 'latitude']]
mypoint = mypoint.as_matrix()
pts = mypoint[:, [2, 3]]


def f(row):
	edict = {}

	x1, x2 = row['nw_lon'], row['se_lon']
	y1, y2 = row['se_lat'], row['nw_lat']
	ll = np.array([x1, y1]) 
	ur = np.array([x2, y2])
	
	inidx = np.all(np.logical_and(ll <= pts, pts <= ur), axis=1)
	pos = np.where(inidx==True)
	
	xx = pd.DataFrame(mypoint[pos]).groupby([0, 1]).count().reset_index()
	for group in eventTypes:
		yy = xx[xx[0] == group].drop([0,3],1)
		zz = list(yy.itertuples(index=False, name=None))
		edict[group] = zz
		
	aa = pd.DataFrame.from_dict(edict, orient='index').as_matrix().tolist()
	return aa   
	
	
	
if __name__ == '__main__':
	p = Pool(3)
	outList = list(p.map(f, tlist))
	fh = open('eventGroups.txt', 'w')
	pickle.dump(outList, fh)
	print time.time() - start_time