# marstech/Marstech-Link-Spray

Application to open many link from one link.

## Getting Started

Download links:

SSH clone URL: ssh://git@git.jetbrains.space/marstech/marstech/Marstech-Link-Spray.git

HTTPS clone URL: https://git.jetbrains.space/marstech/marstech/Marstech-Link-Spray.git

These instructions will get you a copy of the project up and running on your local machine for
development and testing purposes.

## Prerequisites

What things you need to install the software and how to install them.

```
Examples
```

## Deployment

### Reverse proxy apache

Update de SSL version of the apache conf to :

```
ProxyPass / http://localhost:8096/
ProxyPassReverse / http://localhost:8096/
ProxyPreserveHost on
RequestHeader set X-Forwarded-Proto https
RequestHeader set X-Forwarded-Port 443
ProxyPass /.well-known/acme-challenge !
```

## Resources

Add links to external resources for this project

### Bootstrap theme

Patrix theme :

- https://www.codewithpatrick.com/downloads/free-bootstrap-5-portfolio-website-template 

