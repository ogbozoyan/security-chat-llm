# Используем базовый образ Node.js
FROM node:22.9.0

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы проекта
COPY package.json package-lock.json ./
COPY public ./public
COPY src ./src

# Устанавливаем зависимости
RUN npm install

# Сборка проекта
RUN npm run build

# Используем Nginx для статической отдачи файлов
FROM nginx:alpine
COPY --from=0 /app/build /usr/share/nginx/html

# Экспонируем порт
EXPOSE 80

# Запускаем Nginx
CMD ["nginx", "-g", "daemon off;"]
