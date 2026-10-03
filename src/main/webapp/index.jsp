<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="es">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Inicio - Proyecto</title>
        <style>
            body {
                font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                background-color: #f8fafc;
                color: #1e293b;
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                margin: 0;
                padding: 1.5rem;
                box-sizing: border-box;
            }

            .container {
                background-color: #ffffff;
                border-radius: 12px;
                box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
                max-width: 650px;
                width: 100%;
                padding: 2.5rem;
                text-align: center;
            }

            h1 {
                color: #0f172a;
                font-size: 1.75rem;
                margin-bottom: 2rem;
                border-bottom: 2px solid #e2e8f0;
                padding-bottom: 1rem;
            }

            .verse-card {
                background-color: #f1f5f9;
                border-left: 4px solid #2563eb;
                margin: 1.25rem 0;
                padding: 1rem 1.25rem;
                text-align: left;
                border-radius: 0 8px 8px 0;
            }

            .verse-text {
                font-style: italic;
                color: #334155;
                margin: 0 0 0.5rem 0;
                line-height: 1.6;
            }

            .verse-ref {
                font-weight: 600;
                color: #2563eb;
                font-size: 0.9rem;
                margin: 0;
                text-align: right;
            }
        </style>
    </head>

    <body>
        <div class="container">
            <h1>Infraestructura lista para el proyecto</h1>

            <div class="verse-card">
                <p class="verse-text">"Mira que te mando que te esfuerces y seas valiente; no temas ni desmayes, porque
                    Jehová tu Dios estará contigo en dondequiera que vayas."</p>
                <p class="verse-ref">— Josué 1:9</p>
            </div>

            <div class="verse-card">
                <p class="verse-text">"Y todo lo que hagáis, hacedlo de corazón, como para el Señor y no para los
                    hombres."</p>
                <p class="verse-ref">— Colosenses 3:23</p>
            </div>

            <div class="verse-card">
                <p class="verse-text">"Encomienda a Jehová tus obras, y tus pensamientos serán afirmados."</p>
                <p class="verse-ref">— Proverbios 16:3</p>
            </div>

            <div class="verse-card">
                <p class="verse-text">"Todo lo puedo en Cristo que me fortalece."</p>
                <p class="verse-ref">— Filipenses 4:13</p>
            </div>
        </div>
    </body>

    </html>
