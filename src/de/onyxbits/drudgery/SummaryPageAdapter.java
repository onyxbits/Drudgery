package de.onyxbits.drudgery;
import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;


public class SummaryPageAdapter extends PagerAdapter {
	List<View> pages = null;

	public SummaryPageAdapter(List<View> pages) {
		this.pages = pages;
	}

	@Override
	public int getCount() {
		return pages.size();
	}
	
	public View getItemAt(int pos) {
		return pages.get(pos);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public Object instantiateItem(View collection, int position) {
		View v = pages.get(position);
		((ViewPager) collection).addView(v, 0);
		return v;
	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager) collection).removeView((View) view);
	}
}