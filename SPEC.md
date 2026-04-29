# SPEC.md

## Producto

Se quiere usar un producto que actue como un kakebo, es decir, el método japonés de gestión de gastos personales del hogar. Se debe implemtar las distintas páginas de dicho método:

- Pantalla de inicio / Resumen mensual, con los siguientes apartados:
    - Dinero disponible del mes
    - Total gastado hasta la fecha
    - Ahorro previsto vs. ahorro real
    - Alertas visuales (por ejemplo: “has superado el presupuesto en ocio”)
    - Objetivos de ahorro
    - Ingresos
        - Ingreso principal (sueldo)
        - Ingresos extra (freelance, regalos, devoluciones)
        - Opción de ingresos recurrentes
    - Gastos fijos

- Gastos variables
    - upervivencia (comida, transporte)
    - Ocio y caprichos
    - Cultura y educación
    - Extras / imprevistos

- Pantalla de registro de gastos
    - Botón de añadir gastos

## Casos de uso

1. Al entrar mostrar la pantalla de inicio
2. Al pulsar botón añadir gastos ir a la pantalla de gastos variables.
    - Al pulsar en el botón de añadir gastos se debe ir a la pantalla de registro de gastos
    - Una vez guardados los datos debe volver a la pantalla de gastos variables
3. En la pantalla de gastos variables añadir una opción para ir a la pantalla de inicio

## Criterios de aceptación

- Permitir la navegación por la aplicación
- Permitir añadir gastos
- Visualizar nuevos gastos
