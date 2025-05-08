package com.example.csp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csp.FreelancersList.Model.Freelancer;
import com.example.csp.R;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private List<Freelancer> freelancers;
    private OnSearchResultClickListener listener;

    public interface OnSearchResultClickListener {
        void onSearchResultClick(Freelancer freelancer);
    }

    public SearchResultAdapter(List<Freelancer> freelancers, OnSearchResultClickListener listener) {
        this.freelancers = freelancers;
        this.listener = listener;
    }

    public void updateFreelancers(List<Freelancer> newFreelancers) {
        this.freelancers = newFreelancers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Freelancer freelancer = freelancers.get(position);
        holder.nameTextView.setText(freelancer.getName());

        // Display skills if available
        if (freelancer.getSkills() != null && !freelancer.getSkills().isEmpty()) {
            String skills = String.join(", ", freelancer.getSkills());
            holder.skillsTextView.setText(skills);
            holder.skillsTextView.setVisibility(View.VISIBLE);
        } else {
            holder.skillsTextView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSearchResultClick(freelancer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return freelancers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView skillsTextView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            skillsTextView = itemView.findViewById(R.id.skillsTextView);
        }
    }
}