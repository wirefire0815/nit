# nit

Because your brain is bad at math and your phone's calculator won't remember yesterday's hours.

**nit** is a smart work hour calculator designed for flexible schedules and internship core presence requirements. Input your start and end times, get your net hours (with automatic breaks), and see how today impacts the rest of your week. Need to leave early on Friday? **nit** projects exact daily targets to get you out the door on time without violating your compulsory core hours.

nit: nit isn't a timer

## Features

- **Daily Calculation** - Plug in start/end times and get instant net hours with automated break deductions.
- **Smart Weekly Planner Strategy** - Toggle between *Balanced* daily distribution or *Core Target* (early Friday exit) projections.
- **Compulsory Core Hours Validation** - Set mandatory office presence hours (e.g. Mon–Thu 09:30–16:00, Fri 09:30–12:30) and get instant compliance warnings.
- **Smooth ViewPager2 Swiping** - Fluid, windowless horizontal navigation between Home, History, and Settings.
- **Emphasized Weekly Overview** - Prominent weekly hero card with progress bars, remaining target hours, and a 5-day Mon–Fri status grid.
- **Week-by-Week History** - Organized history feed grouped into weekly cards with total hours, days logged, and individual entry management.
- **First-Run Onboarding Setup** - Configure your weekly target hours and core presence preferences right on first launch.
- **Clean Violet/Pink Material 3 Design** - Sleek light grey theme with standardized purple/violet accents, branded app header, and high-visibility vector icons.

## How it works

1. **First Launch**: Set your weekly target (e.g., 38.5h) and toggle compulsory core hours in the onboarding wizard.
2. **Log Your Day**: Pick a date, tap to select start and end times, and see net hours calculated instantly.
3. **Choose Your Strategy**: Select *Balanced* or *Core Target* to calculate when you can leave today—never undercutting your core presence hours.
4. **Track the Week**: View your 5-day Mon–Fri grid and live progress bar update in real time.
5. **Review History**: Swipe over to the History tab to inspect your week-by-week breakdown.

It's a spreadsheet that doesn't require Excel skills. Set your target hours in Settings, then just plug in your times.

## Vibecoded

Originally built by [Mistral Vibe](https://vibe.mistral.ai) and subsequently overhauled by Pair Programming with **Antigravity**. The architecture is clean, the reactivity is instant, the commits are atomic, and no, it still won't start a timer on you.

## Tech Stack

- **Kotlin** - Because Java is for people who enjoy pain
- **AndroidX & ViewPager2** - Modern single-activity tab navigation with `FragmentStateAdapter`
- **Room & KSP** - For local persistence that doesn't suck
- **DataStore** - For user preferences without the SharedPreferences mess
- **Coroutines & StateFlow** - Reactive state synchronization without stale data bleeding
- **Material Design 3** - Crisp violet/pink palette, custom vector icons, and soft rounded cards

## Installation

Clone, open in Android Studio, build, run. You know the drill.

## License

Do whatever you want with it. It's your calculator.
