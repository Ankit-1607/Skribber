print("Detecting Gestures")

import cv2
import mediapipe as mp
import pickle
import numpy as np
import os

# Get the absolute path of the current script
script_dir = os.path.dirname(os.path.abspath(__file__))
model_path = os.path.join(script_dir, 'model.p')


model_dict = pickle.load(open(model_path, 'rb'))
model = model_dict['model']

capture_Img = cv2.VideoCapture(1)
mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles

# model
hands = mp_hands.Hands(static_image_mode=True, min_detection_confidence=0.3)


while True:

  data_aux = []
  x_ = []
  y_ = []

  ret, frame = capture_Img.read()
  if not ret:
    print("Failed to capture image", flush=True)
    continue
  
  H, W, _ = frame.shape

  frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

  results = hands.process(frame_rgb)
  if results.multi_hand_landmarks: # if a hand is detected
    for hand_landmarks in results.multi_hand_landmarks :
      mp_drawing.draw_landmarks(
        frame, #image to draw
        hand_landmarks, #model output
        mp_hands.HAND_CONNECTIONS, #hand connections
        mp_drawing_styles.get_default_hand_landmarks_style(),
        mp_drawing_styles.get_default_hand_connections_style()
      )

    for hand_landmarks in results.multi_hand_landmarks:
       for i in range(len(hand_landmarks.landmark)):
          x = hand_landmarks.landmark[i].x
          y = hand_landmarks.landmark[i].y
          data_aux.append(x)
          data_aux.append(y)
          x_.append(x)
          y_.append(y)

    # Ensure the correct number of features - only 1 hand needs to be tracked
    if len(data_aux) == 42: # 21 landmarks * 2 (x and y points)
      x1 = (int) (min(x_) * W)
      y1 = (int) (min(y_) * H)

      x2 = (int) (max(x_) * W)
      y2 = (int) (max(y_) * H)

      prediction = model.predict([np.asarray(data_aux)])
      print(prediction[0], flush=True)

      cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 0, 0), 4)
      cv2.putText(frame, prediction[0], (x1, y1), cv2.FONT_HERSHEY_SIMPLEX, 1.3, (0, 255, 0), 3, cv2.LINE_AA)
    else:
       print(f"{len(data_aux)} features", flush=True)

  cv2.imshow('frame', frame)
  if cv2.waitKey(800) == ord('q'):  # wait 800ms between each frame
      break

capture_Img.release() # release memory
cv2.destroyAllWindows()