# Keycloak Demo with Spring Boot

This project demonstrates how to integrate Keycloak with Spring Boot using JWT-based OAuth2 Resource Server security.

It supports:
- Keycloak integration (via realms and roles)
- Environment-based configuration (dev/prod)
- GitHub Container Registry (GHCR) build & deployment
- Clean local development using Docker Compose

---
## Requirements

- Java 21 (Temurin or Liberica)
- Docker + Docker Compose
- Maven Wrapper (`./mvnw`)

---

## Local Development & Testing

### Step 1: Start Keycloak (Dev Mode)

```bash
docker compose -f docker-compose.dev.yml up -d
```

This will:
- Start Keycloak on `http://localhost:8080`
- Enable Dev Mode (`start-dev`)
- (Optional) Auto-import realm from `realm-export.json`

Default Keycloak Admin Login  
Username: `admin`  
Password: `admin`

---

### Step 2: (Optional) Export Environment Variables

You only need to export these if you want to override the default values declared in `application.properties`.

```bash
export SPRING_PROFILES_ACTIVE=dev
export JWT_ISSUER_URI=http://localhost:8080/realms/realm-demo
export JWT_JWK_SET_URI=http://localhost:8080/realms/realm-demo/protocol/openid-connect/certs
```

Otherwise, Spring Boot will fallback to the values from `application.properties` automatically.

---

### Step 3: Run Spring Boot App Locally

```bash
./mvnw spring-boot:run
```

By default, the app will run at:  
http://localhost:8080

---

### Step 4: Verify Integration

1. Go to your local Keycloak Admin Console:  
   http://localhost:8080/admin/
2. Log in with `admin / admin`
3. Create the Realms & Users, you may use this link for reference on how to do so  [SpringBoot + KeyCloak intergation](https://medium.com/@iaravinda33/integrating-keycloak-authentication-with-spring-boot-a-complete-guide-98df2c8d244a)
4. Test login and token endpoints:
   - http://localhost:8080/realms/realm-demo/protocol/openid-connect/token
5. Access a secured endpoint in your app using a bearer token

---

## Runtime Profiles

| Profile | Description              | Default URLs                              |
|---------|--------------------------|--------------------------------------------|
| `dev`   | Local development         | http://localhost:8080                    |
| `prod`  | Production (via Traefik) | https://kc.exeltan.com                  |

The active profile is determined by:
1. `SPRING_PROFILES_ACTIVE` environment variable (preferred)
2. Defaults defined in `application.properties`

---

## Production Deployment (GHCR Image)

This project supports OCI image builds via GitHub Actions.

Example Docker Compose service:

```yaml
services:
  keycloak-demo:
    image: ghcr.io/<XXXXXXX>/keycloak-demo:latest
    container_name: keycloak-demo
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JWT_ISSUER_URI=${JWT_ISSUER_URI}
      - JWT_JWK_SET_URI=${JWT_JWK_SET_URI}
    labels:
      - traefik.enable=true
      - traefik.http.routers.keycloak-demo.rule=Host(`<xxxxxx>.com`)
      - traefik.http.routers.keycloak-demo.entrypoints=websecure
      - traefik.http.routers.keycloak-demo.tls=true
      - traefik.http.routers.keycloak-demo.tls.certresolver=myresolver
      - traefik.http.services.keycloak-demo.loadbalancer.server.port=8080
    networks:
      - shared_network
    restart: unless-stopped
```

Never commit production values to source code – always pass them via environment variables or secrets.

---

## Cleanup Local Dev Environment

```bash
docker compose -f docker-compose.dev.yml down -v
```

---

## Developer Tips

- Use `@PreAuthorize("hasRole('admin')")` to secure methods via roles
- Tokens issued by Keycloak will include roles inside the JWT
- Customize JWT claim parsing via `JwtConverter.java`
