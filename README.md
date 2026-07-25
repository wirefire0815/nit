# nit

Because your brain is bad at math and your phone's calculator won't remember yesterday's hours.

**nit** is a simple work hour calculator for flexible schedules. Input your start and end times, get your net hours (with automatic breaks), and see how today affects your weekly total. It calculates daily targets so you know when you can leave without working overtime or undercutting your core presence hours.

nit: nit isn't a timer

## Features

- **Daily calculation** - Plug in start/end times and get instant net hours with automated break deductions.
- **Smart Weekly Planner Strategy** - Toggle between *Balanced* daily distribution or *Core Target* projections.
- **Compulsory Core Hours Validation** - Set mandatory office presence hours and get instant compliance warnings.
- **Smooth Swiping** - Fluid horizontal navigation between Home, History, and Settings.
- **Weekly Overview** - Prominent weekly hero card with progress bars, remaining target hours, and a 5-day Mon–Fri status grid.
- **Week-by-Week History** - Organized history feed grouped into weekly cards with total hours and days logged.
- **Setup Wizard** - Configure your weekly target hours and core presence preferences on first launch.
- **Clean & Modern Look** - Light grey theme with purple/violet accents that doesn't hurt your eyes.

## How it works

1. **First Launch**: Set your weekly target (e.g., 38.5h) and toggle compulsory core hours in the setup wizard.
2. **Log Your Day**: Pick a date, tap to select start and end times, and see net hours calculated instantly.
3. **Choose Your Strategy**: Select *Balanced* or *Core Target* to calculate when you can leave today—never undercutting your core presence hours.
4. **Track the Week**: View your 5-day Mon–Fri grid and live progress bar update in real time.
5. **Review History**: Swipe over to the History tab to inspect your week-by-week breakdown.

It's a spreadsheet that doesn't require Excel skills. Set your target hours in Settings, then just plug in your times.

## Vibecoded

This project was built by [Mistral Vibe](https://vibe.mistral.ai) - an AI coding agent that's surprisingly good at Kotlin and bad at UX design. The architecture is clean, the commits are atomic, and no, it won't start a timer on you.

## Tech Stack

- **Kotlin** - Because Java is for people who enjoy pain
- **AndroidX** - The modern way to do Android
- **Room** - For persistence that doesn't suck
- **DataStore** - For preferences without the SharedPreferences mess
- **Coroutines & Flow** - Because callbacks are so 2015
- **Material Design 3** - So it doesn't look like it was designed in 2010

## Installation

Clone, open in Android Studio, build, run. You know the drill.

## License

Do whatever you want with it. It's your calculator.
