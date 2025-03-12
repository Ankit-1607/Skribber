import os
import mediapipe as mp
import cv2
import matplotlib.pyplot as plt
import pickle

mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles

# model
hands = mp_hands.Hands(static_image_mode=True, min_detection_confidence=0.3)

# Get the absolute path of the current script
script_dir = os.path.dirname(os.path.abspath(__file__))
data_path = os.path.join(script_dir, './data')
DATA_DIR = data_path

data = []
labels = [] # image categories
# iterating thru all the classifier directories
for dir_ in os.listdir(DATA_DIR): 
  # iterate thru all the frames
  for img_path in os.listdir(os.path.join(DATA_DIR, dir_)):
    data_aux = []
    img = cv2.imread(os.path.join(DATA_DIR, dir_, img_path))
    # img convert to RGB matrices to input to mediapipe
    img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

    results = hands.process(img_rgb)

    if results.multi_hand_landmarks: # if a hand is detected
      for hand_landmarks in results.multi_hand_landmarks:
        for j in range(len(hand_landmarks.landmark)):
          x = hand_landmarks.landmark[j].x
          y = hand_landmarks.landmark[j].y
          data_aux.append(x)
          data_aux.append(y)
          
          # print(hand_landmarks.landmark[j]) # position of each landmark, x,y,z

        '''
        # show hand landmarks drawing
        mp_drawing.draw_landmarks(
          img_rgb, #image to draw
          hand_landmarks, #model output
          mp_hands.HAND_CONNECTIONS, #hand connections
          mp_drawing_styles.get_default_hand_landmarks_style(),
          mp_drawing_styles.get_default_hand_connections_style()
        )'
        

    plt.figure()
    plt.imshow(img_rgb)

plt.show()
    '''
      data.append(data_aux)
      labels.append(dir_)

f = open('data.pickle', 'wb')
pickle.dump({'data': data, 'labels':labels}, f)
f.close()