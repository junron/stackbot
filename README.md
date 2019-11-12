# Stackbot
A Discord bot for searching Stack Exchange sites, written in Kotlin with [Diskord](https://github.com/JesseCorbett/Diskord).

## Commands

#### `$ping`
Check if server is alive

#### `$halp [query]`
Search a StackExchange site.   
The site searched depends on the current channel, and defaults to `stackoverflow`.  
See [default sites](#stackExchange-sites-supported)

#### `$[subject name] [query]`
Search the StackExchange site corresponding to `[subject name]`

**Valid subject names:**
- `chem`
- `phys`
- `cs`
- `english`
- `math`

## StackExchange sites supported
### [StackOverflow](https://stackoverflow.com)
Default site for non-subject channels and `cs` channel

### [Chemistry](https://chemistry.stackexchange.com/)
Default site for `chem` channel

### [Physics](https://physics.stackexchange.com/)
Default site for `phys` channel

### [Math](https://math.stackexchange.com/)
Default site for `math` channel

### [English](https://english.stackexchange.com/)
Default site for `english` channel
