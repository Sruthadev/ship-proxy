1. Docker command to run ship proxy - 

docker build -t ship-proxy:latest -f Dockerfile.ship .
docker run -p 8080:8080 ship-proxy:latest

2. Docker command to run offshore proxy -

docker build -t offshore-proxy:latest -f Dockerfile.offshore .
docker run -p 9090:9090 offshore-proxy:latest