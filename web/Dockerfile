FROM node:22.9.0 AS builder

WORKDIR /app

COPY package.json package-lock.json ./
COPY public ./public
COPY src ./src

RUN npm install

RUN npm run build

FROM nginx:alpine as runner
COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
