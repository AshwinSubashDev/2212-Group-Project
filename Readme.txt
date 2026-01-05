Adventure Game Engine Group 30

How to Run

Compile
```
javac -cp "lib/gson-2.10.1.jar;src" -d out src/model/*.java src/View/*.java
```

Run Game
```
java -cp "lib/gson-2.10.1.jar;out" model.UI
```

Run Tests
```
javac -cp "lib/gson-2.10.1.jar;src" -d out src/model/*.java src/test/GameEngineTest.java
java -cp "lib/gson-2.10.1.jar;out" test.GameEngineTest
```

Project Structure
- `src/model/` - Game logic (GameEngine, GameState, ...)
- `src/View/` - UI components
- `src/test/` - Test files
- `lib/` - Dependencies (Gson)