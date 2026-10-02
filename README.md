# lrn-quarkus

## Prerequisites

1. Install quarkus CLI as described here https://quarkus.io/guides/cli-tooling/
2. Install podman - `brew install podman`

## Dev mode

1. Start the podman VM 
    * `podman machine init`
    * `podman machine start`
2. Set the following environment variables:
    ```bash
    export DOCKER_HOST="unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')"
    export TESTCONTAINERS_RYUK_DISABLED=true
    ```
3. `quarkus dev`