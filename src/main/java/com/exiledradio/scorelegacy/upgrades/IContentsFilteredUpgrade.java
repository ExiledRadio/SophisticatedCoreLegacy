package com.exiledradio.scorelegacy.upgrades;

public interface IContentsFilteredUpgrade extends IFilteredUpgrade {
    @Override
    ContentsFilterLogic getFilterLogic();
}
