DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

TAG="latest"

rm -rf build/

./gradlew bootJar -DskipTests

docker container stop $(docker ps -aqf "name=^office-backend")

docker container rm $(docker ps -aqf "name=^office-backend")

docker rmi $(docker images 'tdx-images/office-backend' -q)

docker build -t tqduc1/office-backend:$TAG -f $DIR/Dockerfile $DIR

docker tag tqduc1/office-backend:latest tqduc1/office-backend:$TAG
