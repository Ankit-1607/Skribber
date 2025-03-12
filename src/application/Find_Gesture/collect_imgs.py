import os
import cv2

DATA_DIR = './data'
if not os.path.exists(DATA_DIR):
  os.makedirs(DATA_DIR)

''' 
    classes:
        1 - scroll down, 
        2 - scroll up, 
        3 - next note, 
        4 - previous note, 
        5 - zoom in, 
        6 - zoom out
'''
controls = ['scroll down', 'scroll up', 'next note', 'prev note', 'zoom in', 'zoom out']

number_Of_Classes = 6
dataset_Size = 200

capture_Img = cv2.VideoCapture(1) # use iVCam
for i in range(number_Of_Classes):
  if not os.path.exists(os.path.join(DATA_DIR, controls[i])):
    os.makedirs(os.path.join(DATA_DIR, controls[i]))

  print("Collecting data for class %s" % controls[i])

  done = False

  while True:
    ret, frame = capture_Img.read()
    cv2.putText(frame, 'Ready? Press "R"! ', (100, 50), cv2.FONT_HERSHEY_SIMPLEX, 1.3, (0, 255, 0), 3, cv2.LINE_AA)
    cv2.imshow('frame', frame)
    if cv2.waitKey(25) == ord('r'):
      break

  counter = 0
  while counter < dataset_Size:
    ret, frame = capture_Img.read()
    cv2.imshow('frame', frame)
    cv2.waitKey(25)
    cv2.imwrite(os.path.join(DATA_DIR, controls[i], '{}.jpg'.format(counter)), frame)

    counter += 1

capture_Img.release()
cv2.destroyAllWindows()
