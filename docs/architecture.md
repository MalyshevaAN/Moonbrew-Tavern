# Архитектура

Архитектура Moonbrew Tavern описывает не техническую обвязку Android, а устройство самой игры: какие крупные части в ней есть и как они связаны между собой.

Главная цель архитектуры — сделать проект расширяемым. В игру должно быть удобно добавлять новых посетителей, рецепты, события, улучшения таверны и сюжетные линии.

## Принципы

- Игровая логика отделяется от отображения.
- Контент хранится отдельно от правил игры.
- Сохранение прогресса работает с общим состоянием игры.
- Каждая крупная механика оформляется как отдельная система.
- Android Canvas / Skia и Box2D рассматриваются как инструменты реализации, а не как игровые сущности.

## Основные части проекта

| Часть | Назначение |
| --- | --- |
| Игровое ядро | Управляет текущим состоянием игры и переходами между этапами. |
| Игровые экраны | Показывают таверну, диалоги, приготовление напитков и результаты ночи. |
| Система таверны | Отвечает за уровень таверны, улучшения, репутацию и атмосферу. |
| Система посетителей | Хранит посетителей, их настроение, отношения и личный прогресс. |
| Система диалогов | Управляет репликами, выборами игрока и последствиями ответов. |
| Система напитков | Работает с ингредиентами, рецептами, качеством напитка и мини-игрой. |
| Система событий | Запускает случайные и сюжетные события. |
| Система прогрессии | Открывает новый контент: рецепты, посетителей, события, улучшения. |
| Контент игры | Описания NPC, рецептов, ингредиентов, событий и улучшений. |
| Сохранение | Хранит прогресс игрока между запусками игры. |

## Диаграмма компонентов

```mermaid
flowchart TB
    Core[Игровое ядро]
    Screens[Игровые экраны]

    Tavern[Система таверны]
    Visitors[Система посетителей]
    Dialogue[Система диалогов]
    Brewing[Система напитков]
    Events[Система событий]
    Progression[Система прогрессии]

    Content[(Контент игры)]
    Save[(Сохранение)]

    Core --> Screens

    Core --> Tavern
    Core --> Visitors
    Core --> Dialogue
    Core --> Brewing
    Core --> Events
    Core --> Progression

    Tavern --> Content
    Visitors --> Content
    Dialogue --> Content
    Brewing --> Content
    Events --> Content

    Tavern --> Progression
    Visitors --> Progression
    Dialogue --> Progression
    Brewing --> Progression
    Events --> Progression

    Progression --> Content
    Core --> Save
    Save --> Core
```

## Диаграмма классов

```mermaid
classDiagram
    class GameState["Состояние игры"] {
        +день: Int
        +золото: Int
        +репутация: Int
        +этапИгры: ЭтапИгры
        +сюжетныеФлаги: Set
        +открытыйКонтент: Set
    }

    class Tavern["Таверна"] {
        +название: String
        +уровень: Int
        +атмосфера: АтмосфераТаверны
        +репутация: Int
    }

    class TavernUpgrade["Улучшение таверны"] {
        +id: String
        +название: String
        +описание: String
        +стоимость: Int
        +эффекты: List
    }

    class Visitor["Посетитель"] {
        +id: String
        +имя: String
        +тип: ТипПосетителя
        +характер: String
        +любимыеРецепты: List
    }

    class VisitorState["Состояние посетителя"] {
        +visitorId: String
        +отношение: Int
        +настроение: НастроениеПосетителя
        +этапИстории: Int
    }

    class DialogueTree["Дерево диалога"] {
        +id: String
        +начальныйУзел: String
    }

    class DialogueNode["Узел диалога"] {
        +id: String
        +говорящий: String
        +текст: String
    }

    class DialogueChoice["Выбор в диалоге"] {
        +текст: String
        +следующийУзел: String
        +эффекты: List
    }

    class Inventory["Инвентарь"] {
        +ингредиенты: Map
        +известныеРецепты: Set
    }

    class Ingredient["Ингредиент"] {
        +id: String
        +название: String
        +редкость: РедкостьИнгредиента
        +теги: List
    }

    class Recipe["Рецепт"] {
        +id: String
        +название: String
        +нужныеИнгредиенты: List
        +базоваяЦена: Int
        +эффект: ЭффектНапитка
    }

    class BrewingSession["Приготовление напитка"] {
        +выбранныеИнгредиенты: List
        +результатМиниИгры: Int
        +итоговоеКачество: КачествоНапитка
    }

    class Drink["Напиток"] {
        +название: String
        +качество: КачествоНапитка
        +ингредиенты: List
        +эффекты: List
    }

    class NightEvent["Событие ночи"] {
        +id: String
        +название: String
        +описание: String
        +условия: List
    }

    class EventChoice["Выбор события"] {
        +текст: String
        +эффекты: List
    }

    class SaveData["Данные сохранения"] {
        +состояниеИгры: GameState
        +инвентарь: Inventory
        +состоянияПосетителей: List
    }

    GameState "1" --> "1" Tavern
    GameState "1" --> "1" Inventory
    GameState "1" --> "*" VisitorState
    GameState "1" --> "*" NightEvent

    Tavern "1" --> "*" TavernUpgrade

    Visitor "1" --> "1" VisitorState
    Visitor "1" --> "1" DialogueTree

    DialogueTree "1" --> "*" DialogueNode
    DialogueNode "1" --> "*" DialogueChoice

    Inventory "1" --> "*" Ingredient
    Inventory "1" --> "*" Recipe

    Recipe "1" --> "*" Ingredient
    BrewingSession "1" --> "*" Ingredient
    BrewingSession "1" --> "1" Drink
    Drink "1" --> "*" Ingredient

    NightEvent "1" --> "*" EventChoice

    SaveData "1" --> "1" GameState
    SaveData "1" --> "1" Inventory
    SaveData "1" --> "*" VisitorState
```

Диаграмма классов показывает основные сущности игры и оставляет место для расширения. Например, позже можно добавить классы для достижений, квестов, комнат таверны, редких ингредиентов или отдельных сюжетных линий.
