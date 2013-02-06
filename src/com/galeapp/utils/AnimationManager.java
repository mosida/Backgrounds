package com.galeapp.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

public class AnimationManager {

	public static Animation getAlphaAnimation() {
		// 创建AnimationSet对象
		AnimationSet animationSet = new AnimationSet(true);
		// 从透明度1，到透明度0
		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
		// 动画执行时间
		alphaAnimation.setDuration(1000);
		// 把动画添加到动画集中
		animationSet.addAnimation(alphaAnimation);
		return animationSet;
	}

}
