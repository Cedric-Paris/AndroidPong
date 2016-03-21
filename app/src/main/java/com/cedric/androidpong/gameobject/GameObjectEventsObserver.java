package com.cedric.androidpong.gameobject;

/**
 * Created by Cedric on 10/03/2016.
 */
public interface GameObjectEventsObserver {

    public void onGameObjectNeedToBeDestroyed(GameObject gameObject);
}
