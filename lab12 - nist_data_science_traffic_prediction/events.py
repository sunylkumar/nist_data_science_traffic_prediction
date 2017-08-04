import pandas as pd
from datetime import timedelta
import numpy as np
import time
import datetime
import math
from multiprocessing import Pool

start_time = time.time()

predDF = pd.read_csv("./prediction_trials.tsv", header=0, sep='\t')
eventsDF = pd.read_csv("./events_train.tsv", header=0, sep='\t')

tlist = []
for i in range(len(predDF)):
	tlist.append(predDF.loc[i])

pts = eventsDF[['longitude', 'latitude']]
pts = pts.as_matrix()

def f(row):
    edict = {}
	x1, x2 = row['nw_lon'], row['se_lon']
	y1, y2 = row['se_lat'], row['nw_lat']
	ll = np.array([x1, y1])  # lower-left
	ur = np.array([x2, y2])  # upper-right
	inidx = np.all(np.logical_and(ll <= pts, pts <= ur), axis=1)
	inbox = pts[inidx]
    xx = pd.DataFrame(mypoint[pos]).groupby([0, 1]).count().reset_index()
    for group in eventTypes:
        yy = xx[xx[0] == group].drop([0,3],1)
        z1 = xx[xx[0] == group].drop([0,3],1)[1].tolist()
        z2 = xx[xx[0] == group].drop([0,3],1)[2].tolist()
        edict[group] = [z1,z2]
    
    aa = pd.DataFrame.from_dict(edict, orient='index').as_matrix().tolist()
    return aa   

if __name__ == '__main__':
	p = Pool(2)
	outList = p.map(f, tlist)
	print outList
	print time.time() - start_time