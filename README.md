# Trekka 🏔️

## Tema
Aplicação Móvel para Registo e Partilha de Trilhos

## Descrição
O **Trekka** é uma aplicação móvel nativa desenvolvida em Kotlin que permite aos utilizadores registar e partilhar os seus trilhos de caminhada ou corrida. A aplicação utiliza sensores de hardware para monitorizar o desempenho e integra-se com uma API REST personalizada para garantir a persistência de dados na nuvem e a interação social entre utilizadores.

## 👨‍💻 Autor
* **Nome:** João Joaquim
* **Número:** 24729
* **Curso:** Mestrado em Engenharia Informática - Internet das Coisas (IoT)
* **Unidade Curricular:** Desenvolvimento de Aplicações Móveis Avançadas (DAMA)
* **Ano Letivo:** 2025/2026

## 🚀 Funcionalidades Principais
* **Tracking de Percursos**: Registo em tempo real via GPS com traçado dinâmico no Google Maps.
* **Deteção de Movimento (Sensores)**: Utilização do **Acelerómetro** para identificar automaticamente se o utilizador está parado ou em movimento.
* **Sincronização Cloud**: Sistema de backup e recuperação bidirecional. Os utilizadores podem recuperar o seu histórico ao mudar de dispositivo.
* **Exploração Comunitária**: Consulta de percursos partilhados por outros utilizadores.
* **Sistema de Ratings**: Avaliação de trilhos (1-5 estrelas) com cálculo de média aritmética realizado no servidor.
* **Controlo de Privacidade**: Possibilidade de definir trilhos como Públicos (visíveis para todos) ou Privados.
* **Estatísticas Globais**: Visualização da distância total percorrida e número total de trilhos no histórico.
* **Classificação Inteligente**: Lógica de "IA" que classifica automaticamente a dificuldade do trilho (Fácil/Moderado/Difícil).
* **Internacionalização**: Interface 100% bilingue (**Português** e **Inglês**).
* **Modo Escuro/Claro**: Interface moderna adaptada às preferências do sistema.

## 🛠️ Stack Tecnológica
* **Frontend**: Android Nativo (Kotlin)
* **Base de Dados Local**: Room Database (versão 3)
* **Comunicação**: Retrofit 2 & GSON
* **Backend**: API REST desenvolvida em Node.js e Express (Alojada no **Render**)
* **Base de Dados Cloud**: MongoDB Atlas
* **Mapas**: Google Maps SDK for Android

## 🔑 Credenciais de Teste
* **Utilizador**: `teste@trekka.com` | `Teste123!`
* **Nota**: Se preferir, pode também criar um novo utilizador diretamente na aplicação através do ecrã de registo.

## 📚 Bibliotecas de Terceiros
* `Room`: Persistência de dados local.
* `Retrofit`: Cliente HTTP para integração com a API.
* `Google Play Services`: Localização e Mapas.
* `Kotlin Coroutines`: Gestão de processos assíncronos.
