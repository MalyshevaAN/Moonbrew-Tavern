# Blueprint: Making Moonbrew Tavern More Like a Game

Этот документ фиксирует рабочий черновик следующего этапа развития Moonbrew Tavern: переход от одной жестко заданной демонстрационной сцены к живому игровому циклу с состоянием мира, несколькими посетителями, отношениями, разблокировкой рецептов и экраном входа в таверну.

## Цель этапа

Сейчас в проекте уже есть вертикальный срез:

1. Улица / главный экран.
2. Зал таверны.
3. Диалог.
4. Книга рецептов.
5. Варка.
6. Результат.

Следующий шаг - сделать так, чтобы игра жила между этими экранами:

- золото и репутация должны реально меняться и сохраняться в игровом состоянии;
- в игре должно быть несколько посетителей, а не один сценарный гость;
- отношения с персонажами должны накапливаться;
- ночи должны сменять друг друга;
- рецепты должны открываться постепенно;
- вход в таверну должен стать отдельным игровым действием.

## Главный принцип

Не хранить все в UI и не хардкодить поведение по экранам.

Нужно разделить:

- `content` - статические описания посетителей, рецептов, ингредиентов, запросов и ассетов;
- `state` - текущее состояние прохождения, ночи, отношений, денег и прогрессии.

Это позволит:

- добавлять нового посетителя без переписывания экранов;
- менять вкусы, награды, тексты и изображения в одном месте;
- постепенно переводить игру от демо-сценария к настоящей системе.

## Предлагаемая модель данных

### Живое состояние игры

```kotlin
data class GameState(
  val day: Int,
  val gold: Int,
  val reputation: Int,
  val unlockedRecipeIds: Set<String>,
  val visitorStates: Map<String, VisitorState>,
  val tavern: TavernState,
)

data class TavernState(
  val capacity: Int,
  val occupiedSeats: Int,
)

data class NightState(
  val queueVisitorIds: List<String>,
  val seatedVisitorIds: List<String>,
  val currentVisitorId: String?,
  val phase: NightPhase,
)

enum class NightPhase {
  Entrance,
  Dialogue,
  RecipeBook,
  Brewing,
  Result,
  Summary,
}
```

### Статические описания контента

```kotlin
data class VisitorDefinition(
  val id: String,
  val name: String,
  val title: String,
  val mood: VisitorMood,
  val favoriteTags: Set<FlavorTag>,
  val dislikedTags: Set<FlavorTag>,
  val preferredRecipeIds: Set<String>,
  val requestPool: List<VisitorRequest>,
  val assets: VisitorAssets,
)

data class VisitorState(
  val relationship: Int = 0,
  val timesVisited: Int = 0,
  val unlocked: Boolean = true,
  val storyFlags: Set<String> = emptySet(),
)

data class VisitorAssets(
  val queueRes: Int,
  val tavernSeatRes: Int,
  val dialoguePortraitRes: Int,
  val resultPortraitRes: Int,
)

data class RecipeDefinition(
  val id: String,
  val name: String,
  val description: String,
  val ingredientIds: List<String>,
  val tags: Set<FlavorTag>,
  val unlockRule: UnlockRule,
  val rewardGold: Int,
  val rewardReputation: Int,
)

data class IngredientDefinition(
  val id: String,
  val name: String,
  val rarity: IngredientRarity,
  val flavorTags: Set<FlavorTag>,
  val stockCount: Int,
  val iconRes: Int,
)

data class VisitorRequest(
  val id: String,
  val text: String,
  val desiredTags: Set<FlavorTag>,
  val forbiddenTags: Set<FlavorTag> = emptySet(),
)

enum class FlavorTag {
  Fresh, Warm, Sweet, Herbal, Smoky, Bitter, Bright, Strange
}

sealed interface UnlockRule {
  data object Default : UnlockRule
  data class Reputation(val min: Int) : UnlockRule
  data class Relationship(val visitorId: String, val min: Int) : UnlockRule
  data class DayReached(val day: Int) : UnlockRule
}
```

## Почему контент лучше хранить в Kotlin, а не сразу в JSON

На текущем масштабе проекта лучше использовать Kotlin-каталог контента.

Причины:

- проще редактировать и рефакторить;
- есть подсказки IDE;
- меньше риск сломать формат;
- можно быстро связать контент с `R.drawable`;
- не нужно сейчас тратить время на парсинг и инфраструктуру загрузки.

Позже, когда контента станет намного больше, можно будет рассмотреть JSON или другой внешний формат.

## Предлагаемая структура файлов

```text
android-app/app/src/main/java/com/example/moonbrewtavern/
  domain/model/
    GameState.kt
    NightState.kt
    VisitorDefinition.kt
    VisitorState.kt
    RecipeDefinition.kt
    IngredientDefinition.kt

  data/content/
    ContentCatalog.kt
    visitors/
      LyraVisitor.kt
      MaraVisitor.kt
      GhostVisitor.kt
    recipes/
      StarglowTonicRecipe.kt
      EmberTeaRecipe.kt
    ingredients/
      CommonIngredients.kt

  data/game/
    GameRepository.kt
    DefaultGameRepository.kt

  ui/entrance/
    EntranceScreen.kt
```

## Как описывать посетителей

Посетителя нужно делить на две части:

- `VisitorDefinition` - кто это вообще за персонаж;
- `VisitorState` - что сейчас происходит в прохождении игрока с этим персонажем.

Это важно, потому что:

- имя, вкусы и картинки почти не меняются;
- отношения, число визитов и флаги истории меняются постоянно.

### Пример visitor-файла

```kotlin
package com.example.moonbrewtavern.data.content.visitors

import com.example.moonbrewtavern.R
import com.example.moonbrewtavern.domain.model.*

val lyraVisitor =
  VisitorDefinition(
    id = "lyra",
    name = "Lyra Vale",
    title = "Cartographer of the North Road",
    mood = VisitorMood.Curious,
    favoriteTags = setOf(FlavorTag.Fresh, FlavorTag.Bright, FlavorTag.Warm),
    dislikedTags = setOf(FlavorTag.Strange, FlavorTag.Bitter),
    preferredRecipeIds = setOf("starglow_tonic"),
    requestPool = listOf(
      VisitorRequest(
        id = "lyra_clear_mind",
        text = "Сделай что-нибудь ясное по вкусу, с мягким теплым послевкусием.",
        desiredTags = setOf(FlavorTag.Fresh, FlavorTag.Warm, FlavorTag.Bright),
      ),
    ),
    assets = VisitorAssets(
      queueRes = R.drawable.npc_beard,
      tavernSeatRes = R.drawable.tavern_room_guest_two,
      dialoguePortraitRes = R.drawable.dialogue_visitor_lyra,
      resultPortraitRes = R.drawable.dialogue_visitor_lyra,
    ),
  )
```

## Как описывать рецепты

Рецепт лучше описывать не только через конкретные ингредиенты, но и через вкусовые теги.

Это дает системе гибкость:

- один гость может любить не один конкретный напиток, а целый вкусовой тип;
- можно сравнивать запрос гостя и собранный напиток не только по `recipeId`;
- легче добавлять новые рецепты без ручной прошивки логики под каждого NPC.

### Пример recipe-файла

```kotlin
package com.example.moonbrewtavern.data.content.recipes

import com.example.moonbrewtavern.domain.model.*

val starglowTonicRecipe =
  RecipeDefinition(
    id = "starglow_tonic",
    name = "Starglow Tonic",
    description = "Clean, bright, gently warming tonic for travelers.",
    ingredientIds = listOf("moonmint", "emberzest", "silverfoam"),
    tags = setOf(FlavorTag.Fresh, FlavorTag.Bright, FlavorTag.Warm),
    unlockRule = UnlockRule.Default,
    rewardGold = 7,
    rewardReputation = 2,
  )
```

### Каталог контента

```kotlin
object ContentCatalog {
  val visitors = listOf(lyraVisitor, maraVisitor, ghostVisitor)
  val recipes = listOf(starglowTonicRecipe, emberTeaRecipe)
  val ingredients = listOf(moonmint, emberzest, silverfoam, duskSyrup)
}
```

## Как работать с картинками

Для одного посетителя могут быть разные изображения на разных экранах:

- силуэт или спрайт в очереди;
- спрайт за столом;
- портрет в диалоге;
- портрет или реакция на экране результата.

Поэтому не нужно раскидывать `R.drawable` по UI-коду. Их лучше хранить внутри `VisitorAssets`, а UI должен спрашивать только текущие ассеты у текущего посетителя.

Это даст возможность:

- заменять графику без переписывания экранов;
- постепенно улучшать стиль каждого экрана;
- держать все визуальные ссылки персонажа в одном месте.

## Новый экран входа в таверну

### Идея

Главный экран должен стать не просто декоративной сценой, а началом ночного цикла.

На нем игрок:

- видит очередь у входа;
- понимает, сколько мест осталось в таверне;
- решает, кого впустить, а кому отказать;
- формирует состав гостей в эту ночь.

Это делает игру заметно глубже еще до диалога и варки.

### Что делает `EntranceScreen`

- показывает `nightState.queueVisitorIds`;
- показывает количество свободных мест;
- дает действие `Впустить`;
- дает действие `Отказать`;
- переводит допущенных гостей в `seatedVisitorIds`;
- после этого открывает вход в зал.

### Почему это хорошая механика

- появляется решение до основного обслуживания;
- игрок чувствует управление таверной, а не только последовательность экранов;
- можно вводить гостей, выгодных по золоту, репутации или сюжету;
- можно позже добавить конфликты между гостями, требования по уровню таверны и случайные события.

## Как встроить `EntranceScreen` в навигацию

Текущий поток экранов:

1. Главный экран.
2. Зал таверны.
3. Диалог.
4. Книга рецептов.
5. Варка.
6. Результат.

Предлагаемый поток:

1. `EntranceScreen`
2. `TavernRoomScreen`
3. `DialogueScreen`
4. `RecipeBookScreen`
5. `BrewingScreen`
6. `ResultScreen`
7. `SummaryScreen` позже

### Nav keys

```kotlin
@Serializable data object Entrance : NavKey
@Serializable data object TavernRoom : NavKey
@Serializable data object Dialogue : NavKey
@Serializable data object RecipeBook : NavKey
@Serializable data object Brewing : NavKey
@Serializable data object Result : NavKey
```

### Поведение

- `EntranceScreen` формирует состав ночи;
- `TavernRoomScreen` показывает уже не фиксированных гостей, а гостей из `nightState.seatedVisitorIds`;
- выбор конкретного гостя обновляет `currentVisitorId`;
- дальше экраны диалога, рецепта и варки используют именно текущего гостя и его запрос.

## Репозиторий и игровая логика

Текущий сценарный репозиторий нужно заменить на игровой репозиторий, который умеет работать с состоянием ночи и прогрессией.

### Интерфейс

```kotlin
interface GameRepository {
  val gameState: StateFlow<GameState>
  val nightState: StateFlow<NightState>

  fun startNight()
  fun admitVisitor(visitorId: String)
  fun rejectVisitor(visitorId: String)
  fun selectVisitor(visitorId: String)
  fun evaluateBrew(selectedIngredientIds: Set<String>): BrewResult
  fun applyServingOutcome(result: BrewResult)
  fun finishNight()
}
```

### Основные обязанности репозитория

- генерировать или выбирать очередь гостей на ночь;
- следить за местами в таверне;
- хранить текущего активного гостя;
- считать результат напитка;
- изменять золото, репутацию и отношения;
- открывать рецепты по условиям;
- завершать ночь и переводить игру в следующий день.

## Ближайший практический план

### Этап 1. Подготовить новые модели

Добавить:

- `GameState`
- `NightState`
- `VisitorDefinition`
- `VisitorState`
- `RecipeDefinition`
- `IngredientDefinition`

### Этап 2. Вынести текущий контент в `data/content`

Первым делом вынести:

- текущую Лиру;
- текущие ингредиенты;
- текущий рецепт;
- текущий запрос гостя.

### Этап 3. Переделать репозиторий

Перевести проект с `single scenario` на:

- `gameState`;
- `nightState`;
- методы переходов между фазами.

### Этап 4. Сделать `EntranceScreen`

Минимальная версия:

- очередь из 2-4 гостей;
- ограничение по местам;
- кнопки `впустить` и `отказать`;
- переход в зал.

### Этап 5. Переделать зал на реальные данные

`TavernRoomScreen` должен показывать гостей из состояния, а не статично пришитые картинки.

### Этап 6. Добавить больше контента

Начальный безопасный объем:

- 3 посетителя;
- 3-5 рецептов;
- разные вкусовые теги;
- разные награды;
- базовые отношения.

## Что не нужно делать прямо сейчас

Пока не стоит:

- переносить контент в JSON;
- строить полноценную базу данных;
- делать сложный редактор контента;
- перегружать систему сложными сюжетными графами;
- добавлять сохранения до стабилизации новой модели данных.

Сначала нужно добиться устойчивого игрового цикла с несколькими персонажами и живым состоянием мира.

## Ожидаемый результат этапа

После реализации этого blueprint игра должна ощущаться уже не как цепочка отдельных экранов, а как управляемая таверна:

- игрок решает, кого впускать;
- гости занимают места и ждут обслуживания;
- у гостей разные вкусы и предпочтения;
- напитки реально влияют на отношения, золото и репутацию;
- рецепты открываются по мере прохождения;
- каждая ночь становится немного разной.
