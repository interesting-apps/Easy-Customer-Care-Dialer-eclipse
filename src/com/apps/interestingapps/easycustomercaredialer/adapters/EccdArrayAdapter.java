package com.apps.interestingapps.easycustomercaredialer.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.apps.interestingapps.easycustomercaredialer.R;

public class EccdArrayAdapter<T> extends ArrayAdapter<T> {

	private List<T> originalListObjects;
	private List<T> currentListObjects;
	private Filter stringStartsWithIgnoreCaseFilter;
	private int resource;
	private Context context;
	private String TAG = "EccdArrayAdapter";

	public EccdArrayAdapter(Context context, int resource, List<T> objects) {
		super(context, resource, objects);
		this.context = context;
		this.resource = resource;
		this.originalListObjects = new ArrayList<T>();
		originalListObjects.addAll(objects);
		this.currentListObjects = objects;
	}

	private class StringStartsWithIgnoreCaseFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			// We implement here the filter logic
			if (constraint == null || constraint.length() == 0) {
				// No filter implemented we return all the list
				results.values = originalListObjects;
				results.count = originalListObjects.size();
			} else {
				// We perform filtering operation
				List<T> resultList = new ArrayList<T>();

				for (T obj : originalListObjects) {
					if (obj.toString()
							.toUpperCase()
							.startsWith(constraint.toString().toUpperCase()))
						resultList.add(obj);
				}

				results.values = resultList;
				results.count = resultList.size();

			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {

			// Now we have to inform the adapter about the new list filtered
			if (results.count == 0) {
				currentListObjects.clear();
				// notifyDataSetInvalidated();
				notifyDataSetChanged();
			} else {
				currentListObjects.clear();
				List<T> newListObjects = (List<T>) results.values;
				currentListObjects.addAll(newListObjects);
				notifyDataSetChanged();
			}
		}
	}

	@Override
	public Filter getFilter() {
		if (stringStartsWithIgnoreCaseFilter == null)
			stringStartsWithIgnoreCaseFilter = new StringStartsWithIgnoreCaseFilter();

		return stringStartsWithIgnoreCaseFilter;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		CompanyNameTextViewHolder holder = new CompanyNameTextViewHolder();

		// First let's verify the convertView is not null
		if (convertView == null) {
			// This a new view we inflate the new layout
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(resource, null);
			// Now we can fill the layout with the right values
			TextView tv = (TextView) v.findViewById(R.id.company_name_company_screen_text_view);

			holder.companyNameCompanyScreenTextView = tv;

			v.setTag(holder);
		} else
			holder = (CompanyNameTextViewHolder) v.getTag();

		T p = currentListObjects.get(position);
		holder.companyNameCompanyScreenTextView.setText(p.toString());

		return v;
	}

	private static class CompanyNameTextViewHolder {
		public TextView companyNameCompanyScreenTextView;
	}

}
