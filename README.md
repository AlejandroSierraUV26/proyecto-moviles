# 📃 KnowIt: Aplicación Educativa Basada en Exámenes Interactivos

Este proyecto consiste en una aplicación educativa enfocada en el aprendizaje autodidacta a través de **evaluaciones interactivas**, inspirada en el modelo de Duolingo. Permite a los usuarios practicar temas mediante exámenes de opción múltiple, generando motivación mediante un sistema de rachas y proporcionando recursos educativos adaptativos.

---

## 🔹 Metodología de Desarrollo

El proyecto fue desarrollado siguiendo el enfoque ágil **Kanban**, utilizando tableros para gestionar las tareas.

El equipo se dividió en dos subgrupos:

* **Equipo Backend**: Desarrollo de servicios REST con Ktor, integración de bases de datos, seguridad JWT, consumo de APIs externas e IA.
* **Equipo Frontend**: Desarrollo de la interfaz de usuario en Android, integración con el backend y experiencia de usuario.

---

## 🚀 Tecnologías y Servicios Usados

* **Ktor + Kotlin** para el backend
* **PostgreSQL**, con varias evoluciones estructurales debido al crecimiento del proyecto
* **Android (Kotlin)** para la app móvil
* **Cloudinary** para almacenamiento de imágenes
* **APIs externas**:

  * Google Console (YouTube y Google Scholar)
  * OpenAI API para generación de preguntas por IA

---

## 💡 Objetivos del Proyecto

* Fomentar la práctica y el aprendizaje continuo mediante evaluaciones interactivas.
* Motivar a los usuarios con rachas de estudio y logros.
* Proveer recursos educativos automáticos adaptados al rendimiento del estudiante.
* Permitir generar exámenes personalizados usando IA.

---

## 🔍 Características Principales

* Exámenes interactivos con retroalimentación inmediata
* Sistema de rachas y recompensas para fomentar la constancia
* Recomendación de contenido educativo (YouTube, Google Scholar)
* Generación de preguntas automáticas con IA
* Historial de progreso
* Retroalimentación por pregunta

---

## 💼 Requerimientos Funcionales (RF)

| Código | Título                         | Descripción                              |
| ------ | ------------------------------ | ---------------------------------------- |
| RF-01  | Registro de Estudiantes        | Permitir el registro de nuevos usuarios  |
| RF-02  | Gestión de Perfil              | Permitir editar datos del perfil         |
| RF-03  | Eliminación de Usuarios        | Permitir eliminar cuenta y datos         |
| RF-04  | Recuperación de Contraseña     | Enlace de recuperación por correo        |
| RF-05  | Selección de Materias          | Elegir temas de estudio predefinidos     |
| RF-06  | Exámenes de Opcion Múltiple    | Preguntas con 4 opciones                 |
| RF-07  | Evaluación de Resultados       | Mostrar porcentaje y resultado           |
| RF-08  | Activación de Rachas           | Activar racha al aprobar                 |
| RF-09  | Recomendaciones de Aprendizaje | Sugerencias de YouTube y Scholar si <70% |
| RF-10  | Generación de Preguntas con IA | Ingresar tema y obtener preguntas        |
| RF-11  | Historial de Progreso          | Registro de resultados por tema          |
| RF-12  | Opciones de Retroalimentación  | Mostrar correctas/incorrectas            |

---

## 🛠️ Requerimientos No Funcionales (RNF)

| Código | Título         | Descripción                           |
| ------ | -------------- | ------------------------------------- |
| RNF-01 | Seguridad      | Encriptación, autenticación JWT       |
| RNF-02 | Confiabilidad  | Alta disponibilidad y pocos errores   |
| RNF-03 | Escalabilidad  | Crecimiento sin perder rendimiento    |
| RNF-04 | Rendimiento    | Carga rápida y fluida                 |
| RNF-05 | Usabilidad     | Interfaz intuitiva y accesible        |
| RNF-06 | Mantenibilidad | Código modular, legible y documentado |

---

## 🔹 Estado Actual

> 🚀 Proyecto **completo y funcional**, con pruebas en backend y frontend.

