# Scheduler

It's fairly often that Discord bots need to schedule some task or job for later. To make this simple (and to try to be
accurate about timings), a simple `Scheduler` class is included with Kord Extensions. This class exists to schedule
cancellable delayed `Task` objects, with callbacks.

## Scheduler

For most purposes, you'll want to create an instance of the `Scheduler` class. This class takes no arguments, and is
its own `CoroutineScope`. This allows you to easily manage your scheduled tasks as a whole.


Function     | Description
:----------- | :----------
`callAllNow` | Execute the callback for all registered `Task`s immediately
`schedule`   | Schedule a new `Task` with callback, with a delay specified in seconds or with a `Duration` object from `kx.time`
`shutdown`   | Cancel all scheduled `Task`s and cancel the schedule's coroutine scope, making it unusable

## Task

Task objects contain a callback, as well as scheduling information pertaining to that callback. You normally won't want
to create these yourself - they're managed by `Scheduler` objects - but you can do so if you need to.

Parameter        | Description
:--------------- | :----------
`callback`       | Callback to be executed once enough time has passed
`coroutineScope` | Optional coroutine scope to launch with, defaulting to the current Kord instance
`duration`       | How long to wait after starting before calling the `callback`
`parent`         | Parent `Scheduler` object, if any - `Task` objects remove themselves from their parents on completion
`pollingSeconds` | How long to wait between time checks, in seconds - defaults to `1`

Property         | Description
:--------------- | :----------
`running`        | `true` if the `Task` is currently waiting for time to pass before execution, or if the callback is executing

Function         | Description
:--------------- | :----------
`callNow`        | Stop waiting and immediately call the callback, without launching - `job` will be `null` during execution
`cancel`         | Stop waiting immediately by cancelling the job
`cancelAndJoin`  | Stop waiting, cancelling the job and waiting for it to complete
`join`           | Join the job that's waiting for execution to happen, if any
`shouldStart`    | Calculate whether it's currently time to start the task
`start`          | Mark the current time and start waiting until the `delay` has passed
