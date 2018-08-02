FROM node:9.3.0 as builder
USER node
RUN mkdir ~/.npm-global
ENV NPM_CONFIG_PREFIX=~/.npm-global
#RUN npm install -g @angular/cli@1.6.3

RUN mkdir /home/node/app
WORKDIR /home/node/app
COPY client/package.json /home/node/app/package.json
COPY client/package-lock.json /home/node/app/package-lock.json
RUN npm install
#RUN ls node_modules/@angular-devkit/core

COPY client /home/node/app
RUN $(npm bin)/ng build

FROM nginx
COPY nginx.conf /etc/nginx/nginx.conf
RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /home/node/app/dist /usr/share/nginx/html
