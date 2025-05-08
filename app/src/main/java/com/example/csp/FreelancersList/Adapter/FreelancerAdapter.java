package com.example.csp.FreelancersList.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.csp.FreelancersList.Model.Freelancer;
import com.example.csp.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class FreelancerAdapter extends RecyclerView.Adapter<FreelancerAdapter.FreelancerViewHolder> {

    private Context context;
    private List<Freelancer> freelancers;
    private static final String TAG = "FreelancerAdapter";
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Freelancer freelancer);
    }

    public FreelancerAdapter(Context context, List<Freelancer> freelancers, OnItemClickListener listener) {
        this.context = context;
        this.freelancers = freelancers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FreelancerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_freelancer_list, parent, false);
        return new FreelancerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FreelancerViewHolder holder, int position) {
        Freelancer freelancer = freelancers.get(position);

        holder.nameText.setText(freelancer.getName());
        holder.experienceText.setText(freelancer.getExperience() + " years experience");
        holder.hourlyRateText.setText("₹" + freelancer.getHourlyRate() + "/hr");
        float avgRating = freelancer.getAverageRating();
        holder.ratingBar.setRating(avgRating);

        LayerDrawable stars = (LayerDrawable) holder.ratingBar.getProgressDrawable();

        if (avgRating > 0) {
            stars.getDrawable(2).setColorFilter(ContextCompat.getColor(context, R.color.yellow), PorterDuff.Mode.SRC_ATOP); // Filled stars
            stars.getDrawable(1).setColorFilter(ContextCompat.getColor(context, R.color.yellow), PorterDuff.Mode.SRC_ATOP); // Half-filled stars
            stars.getDrawable(0).setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_ATOP); // Empty stars
        } else {
            stars.getDrawable(2).setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(1).setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(ContextCompat.getColor(context, R.color.gray), PorterDuff.Mode.SRC_ATOP);
        }

        if (freelancer.getProfileImageUrl() != null && !freelancer.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(freelancer.getProfileImageUrl())
                    .placeholder(R.drawable.baseline_person_24)
                    .error(R.drawable.baseline_person_24)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.baseline_person_24);
        }

        holder.skillsChipGroup.removeAllViews();
        if (freelancer.getSkills() != null) {
            for (String skill : freelancer.getSkills()) {
                Chip chip = (Chip) LayoutInflater.from(context)
                        .inflate(R.layout.item_skill, holder.skillsChipGroup, false);
                chip.setText(skill);
                holder.skillsChipGroup.addView(chip);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(freelancer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return freelancers != null ? freelancers.size() : 0;
    }

    static class FreelancerViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profileImage;
        TextView nameText, experienceText, hourlyRateText;
        RatingBar ratingBar;
        ChipGroup skillsChipGroup;

        FreelancerViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            nameText = itemView.findViewById(R.id.nameText);
            experienceText = itemView.findViewById(R.id.experienceText);
            hourlyRateText = itemView.findViewById(R.id.hourlyRateText);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            skillsChipGroup = itemView.findViewById(R.id.skillsChipGroup);
        }
    }
}