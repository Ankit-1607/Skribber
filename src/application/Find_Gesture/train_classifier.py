import pickle
import numpy as np
import os

from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score

# Get the absolute path of the current script
script_dir = os.path.dirname(os.path.abspath(__file__))
data_path = os.path.join(script_dir, 'data.pickle')

# train model using random forest classifier-fast and good for non-complex and large datasets

data_dict = pickle.load(open(data_path, 'rb'))

data = np.asarray(data_dict['data'])
labels = np.asarray(data_dict['labels'])

# stratify=labels keeps same proportion of data for training of test for each label
x_train, x_test, y_train, y_test = train_test_split(data, labels, test_size=0.2, shuffle=True, stratify=labels)

model = RandomForestClassifier()
model.fit(x_train, y_train)

y_predict = model.predict(x_test)

score = accuracy_score(y_predict, y_test)
print('{}% of samples were classified correctly!'.format(score*100))

f = open('model.p', 'wb')
pickle.dump({'model':model}, f)
f.close()