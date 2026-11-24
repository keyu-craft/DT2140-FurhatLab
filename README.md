# DT2140 Furhat Lab — Multimodal Spoken Dialogue Skills

This repository contains four Kotlin skills built with **Furhat SDK 3.0** for the DT2140 Multimodal Interaction lab.

## Skills Included

1. **`JobInterviewer` (Task 1):**
   A simulated job interviewer with a custom persona (`Danielle` / `Marty`), structured interview prompt, interruption support (`furhat.interruptable = true`), automatic turn-taking gestures, LLM-generated openings, and tool-based conversation ending (`EndConversation` + `furhat.users.disengage()`).

2. **`WeatherGuide` (Task 2):**
   A weather assistant using `functionTool<GeoLocation>` and the **Open-Meteo API** (`AskWeatherToday`, `AskWeatherForecast`) to answer current weather and forecast queries, supporting elliptical follow-ups and multi-city comparisons in a single turn.

3. **`HotelReceptionist` (Task 3):**
   A two-state (`Receptionist`, `Manager`) and two-generator (`receptionistGenerator`, `managerGenerator`) interaction. The receptionist handles reservation lookups via a hard-coded `FindReservation` tool and escalates to the manager (`TalkToManager`) with a distinct face and voice (`Matthew` / `Alex`) while preserving dialogue history.

4. **`DutyFreeShop` (Task 4 — Custom Extension):**
   A Swedish airport duty-free checkout scenario addressing Dynamic Currency Conversion (DCC). Combines the **Frankfurter Currency API** (`CheckExchangeRate`) with a two-persona escalation (`NewEmployee` → `SeniorEmployee`) when the customer insists on paying in SEK instead of the POS terminal's USD conversion.

## How to Run

1. Start **Furhat SDK Launcher**, click **Launch Virtual Furhat**, and open the web dashboard (`localhost:8080`, default password `admin`).
2. Open the target skill folder's `build.gradle` in **IntelliJ IDEA** as a Gradle project.
3. Create `src/main/resources/.env` inside the skill folder and add your OpenAI API key:
   ```env
   OPENAI_API_KEY=your_api_key_here
   ```
4. Run `main.kt` from IntelliJ IDEA and place a virtual user in the inner interaction zone on the dashboard.
