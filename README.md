# Ship Proxy and Offshore Proxy

## Docker Setup

### Build Docker Images


Build and run Offshore Proxy image:

```bash
docker build -t offshore-proxy:latest -f Dockerfile.offshore .
docker run -p 9090:9090 offshore-proxy:latest
```


Build and run Ship Proxy image:

```bash
docker build -t ship-proxy:latest -f Dockerfile.ship .
docker run --network host ship-proxy:latest
```