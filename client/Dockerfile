FROM node:9.3.0 as builder
USER node
RUN mkdir ~/.npm-global
ENV NPM_CONFIG_PREFIX=~/.npm-global
RUN npm install -g @angular/cli

RUN mkdir /home/node/app
WORKDIR /home/node/app
COPY client/package.json /home/node/app/package.json
RUN npm install

COPY client /home/node/app
#RUN npm install

RUN $(npm bin)/ng build

FROM nginx
COPY nginx.conf /etc/nginx/nginx.conf
RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /home/node/app/dist /usr/share/nginx/html