package com.example.csp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csp.R;

import java.util.List;

public class SkillsAdapter extends RecyclerView.Adapter<SkillsAdapter.ViewHolder> {

    private List<String> skills;

    public SkillsAdapter(List<String> skills) {
        this.skills = skills;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_skill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String skill = skills.get(position);
        holder.skillText.setText(skill);
    }

    @Override
    public int getItemCount() {
        return skills.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView skillText;

        ViewHolder(View itemView) {
            super(itemView);
            skillText = itemView.findViewById(R.id.skillText);
        }
    }
}

