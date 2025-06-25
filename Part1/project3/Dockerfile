FROM node:18

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY controllers ./controllers
COPY middleware/ middleware/
COPY routes ./routes
COPY models ./models
COPY services ./services
COPY app.js .

EXPOSE 4000

CMD ["node", "app.js"]