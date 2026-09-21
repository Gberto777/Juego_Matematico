# MathQuest — Manual de usuario

Guía paso a paso para usar la app móvil de MathQuest: iniciar sesión,
desbloqueo biométrico, y registrar/editar/eliminar tu progreso.

---

## 1. Iniciar sesión

1. Abre la app. La primera pantalla es **Iniciar sesión**.
2. Escribe tu **correo electrónico** en el primer campo.
3. Escribe tu **contraseña** en el segundo campo (el texto se muestra
   oculto, como puntos, por seguridad).
4. Pulsa el botón **"Iniciar sesión"**.
   - Mientras se valida, el botón muestra un pequeño indicador de
     carga y los campos se deshabilitan.
   - Si las credenciales son correctas, pasas automáticamente a la
     pantalla **Mi progreso en MathQuest**.
   - Si el correo o la contraseña son incorrectos, verás un mensaje de
     error debajo de los campos (y una notificación tipo Snackbar en
     la parte inferior de la pantalla) indicando que las credenciales
     son inválidas. Corrige y vuelve a intentar.
   - Si no hay conexión (sin red, servidor caído, o la conexión tarda
     demasiado), verás el mensaje **"No hay conexión a Internet."**.
     Revisa tu conexión Wi-Fi/datos y vuelve a intentar.

> Ambos campos son obligatorios: si intentas iniciar sesión con alguno
> vacío, verás un aviso pidiéndote completarlos, sin necesidad de
> contactar al servidor.

---

## 2. Desbloqueo biométrico

Si tu dispositivo tiene huella digital o reconocimiento facial
configurado, puedes usarlo como alternativa a escribir tu contraseña:

1. En la pantalla de **Iniciar sesión**, pulsa el botón secundario
   **"Desbloqueo Biométrico"** (debajo del botón principal de
   "Iniciar sesión", con un estilo visualmente distinto).
2. El sistema operativo mostrará su propio diálogo de seguridad
   (huella, rostro, o el método biométrico configurado en tu equipo).
3. Coloca tu huella o mira a la cámara según lo que pida el diálogo.
   - Si el sistema reconoce tu biometría, entras directamente a la
     pantalla de progreso, igual que con un login exitoso.
   - Si no la reconoce (huella equivocada), el propio diálogo del
     sistema te permite reintentar.
   - Si prefieres no usar biometría en ese momento, el diálogo tiene
     un botón **"Usar contraseña"**; al pulsarlo (o si tu dispositivo
     no tiene biometría configurada), la app te lo indica y puedes
     iniciar sesión normalmente con el correo y la contraseña.

> La app **nunca** guarda ni procesa tu huella o rostro: todo el
> reconocimiento biométrico ocurre dentro del sistema operativo de tu
> dispositivo (Android), de forma segura.

---

## 3. Registrar un nuevo avance de progreso

Una vez dentro de **Mi progreso en MathQuest**:

1. Verás una lista con tus avances anteriores (nivel alcanzado y
   puntaje de cada uno, con la fecha). Si es tu primera vez, la lista
   estará vacía con un mensaje invitándote a agregar tu primer
   registro.
2. Pulsa el botón flotante circular con el símbolo **"+"** (esquina
   inferior derecha).
3. Se abre un formulario emergente **"Registrar progreso"** con dos
   campos:
   - **Nivel alcanzado**: un número entero de 1 en adelante.
   - **Puntaje**: un número entero de 0 en adelante.
4. Escribe ambos valores y pulsa **"Guardar"**.
   - El botón "Guardar" solo se activa cuando ambos valores son
     válidos (números enteros dentro del rango permitido).
   - Mientras se envía, el botón muestra "Guardando…".
   - Si todo sale bien, el diálogo se cierra y tu nuevo registro
     aparece al principio de la lista.
   - Si hay un problema de conexión o del servidor, verás el aviso
     correspondiente ("No hay conexión a Internet." o "Existe un
     error.") y el registro no se guarda; puedes volver a intentarlo.
5. Para cancelar sin guardar, pulsa **"Cancelar"**.

---

## 4. Editar un avance existente

1. En la lista de progreso, cada tarjeta tiene dos íconos a la
   derecha: un **lápiz** (editar) y una **papelera** (eliminar).
2. Pulsa el ícono de **lápiz** del registro que quieras modificar.
3. Se abre el mismo formulario que para registrar, pero con el título
   **"Editar progreso"** y los campos **ya rellenos** con los valores
   actuales de ese registro.
4. Cambia el **Nivel alcanzado** y/o el **Puntaje** según necesites.
5. Pulsa **"Guardar"**.
   - Si la actualización tiene éxito, la tarjeta en la lista se
     actualiza con los nuevos valores y la fecha se refresca
     automáticamente (la calcula el servidor).
   - Si el registro ya no existe (por ejemplo, lo eliminaste desde
     otro dispositivo mientras tanto), verás un aviso indicando que
     ese registro ya no está disponible.
   - Igual que al registrar, un problema de conexión muestra el
     mensaje de error correspondiente y puedes reintentar.

---

## 5. Eliminar un avance

1. En la lista de progreso, pulsa el ícono de **papelera** del
   registro que quieras borrar.
2. Aparece un diálogo de confirmación: **"¿Seguro que quieres eliminar
   el registro de Nivel X · Y pts? Esta acción no se puede deshacer."**
3. Pulsa **"Eliminar"** para confirmar, o **"Cancelar"** para no hacer
   nada.
4. Si confirmas, el registro se borra del servidor y desaparece
   inmediatamente de la lista.
   - Si el registro ya no existía (por ejemplo, ya lo habías borrado
     antes desde otra sesión), verás un aviso indicándolo.
   - Si hay un problema de conexión, verás el mensaje correspondiente
     y el registro no se elimina; puedes volver a intentarlo cuando
     tengas conexión.

---

## Sobre los mensajes de error

En cualquier pantalla, si algo falla al comunicarse con el servidor
verás uno de estos dos mensajes (como texto en la pantalla y/o como
una notificación breve en la parte inferior):

- **"No hay conexión a Internet."** — tu dispositivo no pudo
  comunicarse con el servidor (sin red, Wi-Fi/datos apagados, o el
  servidor no responde). Revisa tu conexión y vuelve a intentar.
- **"Existe un error."** — ocurrió un problema inesperado que no es
  de conectividad. Vuelve a intentar la acción; si persiste, contacta
  a soporte.
