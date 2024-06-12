docker build -t iot-app:back .
docker save -o /Users/dfurchert/Documents/informatik/Terrarium/orchestrationIotApp/back-iot-image iot-app:back
scp -P 22 /Users/dfurchert/Documents/informatik/Terrarium/orchestrationIotApp/back-iot-image dfurchert@192.168.1.163:/home/dfurchert/iotApp/back-iot-image