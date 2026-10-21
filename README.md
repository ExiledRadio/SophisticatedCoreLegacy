# Sophisticated Core Legacy

The shared library of the **unofficial** 1.12.2 (Forge 14.23.5) ports of
[Sophisticated Backpacks](https://github.com/P3pp3rF1y/SophisticatedBackpacks) and
[Sophisticated Storage](https://github.com/P3pp3rF1y/SophisticatedStorage): the upgrade system,
the storage GUI framework, filters, network sync and the liquid experience fluid. It adds no
items by itself; Sophisticated Backpacks Legacy and Sophisticated Storage Legacy register their
own upgrade items built from it.

## Credits and license

Sophisticated Core, Sophisticated Backpacks and Sophisticated Storage are by **P3pp3rF1y**. This
library was split out of Sophisticated Backpacks Legacy, which is ported from Sophisticated
Backpacks' `1.16.x` branch at commit
[`c650e8a`](https://github.com/P3pp3rF1y/SophisticatedBackpacks/tree/c650e8a4248351b85537b285a0cc8b5f15a4a4f5)
(before Sophisticated Core existed as its own mod), with the structure of Sophisticated Core's
`1.18.x` branch at commit
[`42394d4`](https://github.com/P3pp3rF1y/SophisticatedCore/tree/42394d4f8f006d7bc6cb24ce180db2c3a75ae62e).
Both were released under the GNU General Public License v3. The infinity upgrade and fractional stack upgrades follow Sophisticated Core's `1.20.x` branch at
[`91ffbe1`](https://github.com/P3pp3rF1y/SophisticatedCore/tree/91ffbe166e68ac0a5eddb4c4ca254b9d40073c67) (February 2025, also GPLv3).

This port is a modified version of that work and is also licensed under the **GNU GPL v3**; see
[LICENSE](LICENSE). It isn't affiliated with or supported by P3pp3rF1y. Please don't report issues
with this port to the original mods.

## Building

```
gradlew build copyToDependents
```

The jar is written to `build/libs/`, and `copyToDependents` puts it into the `deps/` folder of
Backpacks Legacy and Storage Legacy (if those workspaces are next to this one).
