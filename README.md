Мод для Minecraft, который добавляет кастомную функцию плотности (density function), управляемую полутоновыми картами высот. Это позволяет использовать черно-белые изображения для точного формирования рельефа, контроля генерации мира и создания пользовательских массивов суши или структур на основе карт высот.

# Возможности

• Загрузка карт из PNG(grayscale)

• JSON-конфиг на каждую карту (mode, min/max, scale)

• Бесконечный тайлинг (карта повторяется по миру)


### Два режима работы:

• "gradient" — линейно переводит яркость 0..255 в диапазон [min..max]

• "steps" — вы сами назначаете значения цветам

# Как пользоваться
Вам нужно будет сделать датапак и добавить его в мир

## Структура:
### 1. Индекс карт
   Хранит список изображений, которые требуется загрузить. Обязательно располагать именно по пути ниже.
> data/worldbuilder/worldbuilder/maps/index.json
```json
{
  "maps": [
    "<namespace>:<id>"
  ]
}
```

### 2. Хранилище карт
> data/<namespace>/worldbuilder/maps/
Здесь лежат пары карты и json конфига. Название файла - его id.
#### Пример конфига для gradient:
```json
{
  "mode": "gradient",
  "min": -1.0,
  "max": 1.0,
  "scale": 4
}
```
Данный режим полезен, если у вас есть готовая карта высот. Самое чёрное значение(0) преобразуется в min, самое белое(255) в max, и далее используется функцией шума.
#### Пример конфига для steps:
```json
{
  "mode": "steps",
  "scale": 2,
  "steps": {
    "0": 100.0,
    "174": 140.0,
    "gray": value
  }
}
```
Данный режим полезен, если вы хотите влиять на Temperature, Humidity, Continentalness и т.д. У этих параметров есть уровни, которые делают мягкий переход не таким важным. Согласно Вики:
> If the continentalness of a location is between -1.2 --1.05, the mushroom fields biome is generated; when it is between -1.05 - -0.455, deep ocean biomes are generated; when -0.455 - -0.19, ocean biomes are generated; when -0.19 - 1.0, inland biomes are generated. For inland biomes, continentalness values are further subdivided into 4 types: coast (-0.19 - -0.11), near-inland (-0.11 - 0.03), mid-inland (0.03 - 0.3) and far-inland (0.3 - 1.0).

### 3. Регистрация DensityFunction
Вы можете регистрировать функцию прямо в NoiseSettings, можете сделать отдельный json и потом ссылаться на него
> data/<namespace>/worldbuilder/maps/worldgen/density_function/Function_name.json
```json
{
  "type": "worldbuilder:grayscale_map",
  "map": "<namespace>:id"
}
```
### В релизах вы можете найти тестовый датапак


# Лицензия
LGPL-3.0.
