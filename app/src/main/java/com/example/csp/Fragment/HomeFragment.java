package com.example.csp.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.csp.Adapter.HorizontalAdapter;
import com.example.csp.Adapter.SearchResultAdapter;
import com.example.csp.FreelancerProfileActivity;
import com.example.csp.FreelancersList.FreelancersListActivity;
import com.example.csp.FreelancersList.Model.Freelancer;
import com.example.csp.FreelancersList.Adapter.FreelancerAdapter;
import com.example.csp.Model.ServiceItem;
import com.example.csp.R;
import com.example.csp.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment implements HorizontalAdapter.OnItemClickListener, SearchResultAdapter.OnSearchResultClickListener {

    private RecyclerView popularFreelancersRecyclerView;
    private RecyclerView recyclerView;
    private FragmentHomeBinding binding;
    private Spinner sortSpinner, filterSpinner;
    private FreelancerAdapter adapter;
    private List<Freelancer> freelancerList = new ArrayList<>();
    private List<Freelancer> allFreelancers = new ArrayList<>();
    private DatabaseReference freelancersRef;
    private RecyclerView searchResultsRecyclerView;
    private SearchResultAdapter searchResultAdapter;
    private EditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerViews();
        loadFreelancers();
        setupSpinners();
        setupImageSlider();
        setupHorizontalScrolling();
        setupSearchFunctionality();
    }

    private void initializeViews(View view) {
        popularFreelancersRecyclerView = view.findViewById(R.id.popularFreelancersRecyclerView);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        sortSpinner = view.findViewById(R.id.sortSpinner);
        filterSpinner = view.findViewById(R.id.filterSpinner);
        searchEditText = view.findViewById(R.id.searchEditText);
        recyclerView = view.findViewById(R.id.horizontalScrolling);
        freelancersRef = FirebaseDatabase.getInstance().getReference().child("freelancers");
    }

    private void setupRecyclerViews() {
        // Search Results RecyclerView
        searchResultAdapter = new SearchResultAdapter(new ArrayList<>(), this);
        searchResultsRecyclerView.setAdapter(searchResultAdapter);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchResultsRecyclerView.setVisibility(View.GONE);

        // Popular Freelancers RecyclerView
        popularFreelancersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FreelancerAdapter(getContext(), freelancerList, this::onFreelancerClick);
        popularFreelancersRecyclerView.setAdapter(adapter);
    }

    private void onFreelancerClick(Freelancer freelancer) {
        Intent intent = new Intent(getActivity(), FreelancerProfileActivity.class);
        intent.putExtra("userId", freelancer.getId());
        startActivity(intent);
    }

    private void setupSpinners() {
        setupSortSpinner();
        setupFilterSpinner();
    }

    private void setupImageSlider() {
        ArrayList<SlideModel> imageList = new ArrayList<>();
        imageList.add(new SlideModel(R.drawable.banner, ScaleTypes.FIT));
        imageList.add(new SlideModel(R.drawable.slider2, ScaleTypes.FIT));
        imageList.add(new SlideModel(R.drawable.slider3, ScaleTypes.FIT));

        ImageSlider imageSlider = binding.imageSlider;
        imageSlider.setImageList(imageList, ScaleTypes.FIT);

        imageSlider.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemSelected(int position) {
                Intent intent = new Intent(getContext(), FreelancersListActivity.class);
                // Add a flag to indicate this is coming from the image slider
                intent.putExtra("fromImageSlider", true);
                // Explicitly set selectedSkill to null to prevent title from being set
                intent.putExtra("selectedSkill", "");
                startActivity(intent);
            }

            @Override
            public void doubleClick(int position) {
                // Handle double click if needed (optional)
            }
        });
    }

    private void setupHorizontalScrolling() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<ServiceItem> serviceItems = new ArrayList<>();
        serviceItems.add(new ServiceItem(R.drawable.web, "Web Development", "Build and optimize stunning websites tailored to your needs."));
        serviceItems.add(new ServiceItem(R.drawable.video_editing, "Video Editing", "Team up with professional video editors to create stunning visuals for your projects!"));
        serviceItems.add(new ServiceItem(R.drawable.graphic_designing, "Graphic Design", "Connect with expert graphic designers who will help your brand stand out."));
        serviceItems.add(new ServiceItem(R.drawable.ui, "UI/UX Design", "Create intuitive and visually engaging user experiences."));

        HorizontalAdapter adapter = new HorizontalAdapter(serviceItems);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(ServiceItem item) {
        Intent intent = new Intent(getActivity(), FreelancersListActivity.class);
        intent.putExtra("selectedSkill", item.getTitle());
        startActivity(intent);
    }

    private void loadFreelancers() {
        freelancersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allFreelancers.clear();
                freelancerList.clear();
                List<Freelancer> tempList = new ArrayList<>();

                for (DataSnapshot freelancerSnapshot : snapshot.getChildren()) {
                    String userId = freelancerSnapshot.getKey();
                    if (userId == null) continue;

                    Freelancer freelancer = freelancerSnapshot.getValue(Freelancer.class);
                    if (freelancer != null) {
                        freelancer.setId(userId);

                        // Load skills
                        List<String> skillsList = new ArrayList<>();
                        if (freelancerSnapshot.hasChild("skills")) {
                            for (DataSnapshot skillSnapshot : freelancerSnapshot.child("skills").getChildren()) {
                                String skill = skillSnapshot.getValue(String.class);
                                if (skill != null) {
                                    skillsList.add(skill);
                                }
                            }
                        }
                        freelancer.setSkills(skillsList);

                        loadUserData(freelancer, tempList, snapshot.getChildrenCount());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Database error: " + error.getMessage());
            }
        });
    }

    private void loadUserData(Freelancer freelancer, List<Freelancer> tempList, long totalCount) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(freelancer.getId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    String name = userSnapshot.child("name").getValue(String.class);
                    freelancer.setName(name != null ? name : "Unknown");
                    tempList.add(freelancer);

                    if (tempList.size() == totalCount) {
                        updateFreelancerList(tempList);
                    }
                } else {
                    Log.w("FirebaseDebug", "User data not found for ID: " + freelancer.getId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching user data: " + error.getMessage());
            }
        });
    }

    private void updateFreelancerList(List<Freelancer> tempList) {
        freelancerList.clear();
        freelancerList.addAll(tempList);
        allFreelancers.addAll(tempList);
        adapter.notifyDataSetChanged();
    }

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.sort_options, R.layout.spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setSelection(0, false);

        sortSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedSort = parent.getItemAtPosition(position).toString();
                sortFreelancers(selectedSort);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                sortSpinner.setSelection(0, true);
            }
        });
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.filter_options, R.layout.spinner_item);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setSelection(0, false);

        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                filterFreelancers(selectedFilter);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                filterSpinner.setSelection(0, true);
            }
        });
    }

    private void sortFreelancers(String criteria) {
        switch (criteria) {
            case "Ratings: High to Low":
                Collections.sort(freelancerList, Comparator.comparingDouble(Freelancer::getAverageRating).reversed());
                break;
            case "Experience: High To Low":
                Collections.sort(freelancerList, Comparator.comparingInt(Freelancer::getExperience).reversed());
                break;
            case "HourlyRate: Low to High":
                Collections.sort(freelancerList, Comparator.comparingInt(Freelancer::getHourlyRate));
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private void filterFreelancers(String category) {
        freelancerList.clear();

        if (category.equals("Filter")) {
            freelancerList.addAll(allFreelancers);
        } else {
            Set<Freelancer> filteredSet = new HashSet<>();  // Use HashSet to avoid duplicates

            for (Freelancer freelancer : allFreelancers) {
                if (freelancer.getSkills() != null && freelancer.getSkills().contains(category)) {
                    filteredSet.add(freelancer);
                }
            }
            freelancerList.addAll(filteredSet);  // Convert Set to List (removes duplicates)
        }
        adapter.notifyDataSetChanged();
    }

    private void setupSearchFunctionality() {
        // Setup search edit text
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    launchFreelancersListActivity(query);
                    return true;
                }
            }
            return false;
        });

        // Setup live search results
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase().trim();
                if (query.isEmpty()) {
                    searchResultsRecyclerView.setVisibility(View.GONE);
                } else {
                    List<Freelancer> filteredFreelancers = filterFreelancersByQuery(query);
                    searchResultAdapter.updateFreelancers(filteredFreelancers);
                    searchResultsRecyclerView.setVisibility(filteredFreelancers.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
        });

        // Setup click outside to dismiss search results
        View rootView = getView();
        if (rootView != null) {
            rootView.setOnClickListener(v -> searchResultsRecyclerView.setVisibility(View.GONE));
        }
    }

    private void launchFreelancersListActivity(String query) {
        Intent intent = new Intent(getActivity(), FreelancersListActivity.class);
        intent.putExtra("searchQuery", query);
        startActivity(intent);
        searchEditText.setText("");
        searchResultsRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onSearchResultClick(Freelancer freelancer) {
        launchFreelancersListActivity(freelancer.getName());
    }

    private List<Freelancer> filterFreelancersByQuery(String query) {
        List<Freelancer> filteredList = new ArrayList<>();
        String queryLower = query.toLowerCase().trim();

        // First pass: collect all matching freelancers with relevance scores
        List<FreelancerSearchResult> searchResults = new ArrayList<>();

        for (Freelancer freelancer : allFreelancers) {
            int relevanceScore = 0;
            boolean nameMatch = false;
            boolean skillMatch = false;

            // Check name match (highest priority)
            if (freelancer.getName() != null) {
                String nameLower = freelancer.getName().toLowerCase();
                if (nameLower.equals(queryLower)) {
                    // Exact name match (highest score)
                    relevanceScore += 1000;
                    nameMatch = true;
                } else if (nameLower.startsWith(queryLower)) {
                    // Name starts with query
                    relevanceScore += 500;
                    nameMatch = true;
                } else if (nameLower.contains(queryLower)) {
                    // Name contains query
                    relevanceScore += 200;
                    nameMatch = true;
                }
            }

            // Check skill match (secondary priority)
            if (freelancer.getSkills() != null) {
                for (String skill : freelancer.getSkills()) {
                    if (skill != null) {
                        String skillLower = skill.toLowerCase();
                        if (skillLower.equals(queryLower)) {
                            // Exact skill match
                            relevanceScore += 100;
                            skillMatch = true;
                        } else if (skillLower.startsWith(queryLower)) {
                            // Skill starts with query
                            relevanceScore += 50;
                            skillMatch = true;
                        } else if (skillLower.contains(queryLower)) {
                            // Skill contains query
                            relevanceScore += 20;
                            skillMatch = true;
                        }
                    }
                }
            }

            // Add to results if there was any match
            if (nameMatch || skillMatch) {
                searchResults.add(new FreelancerSearchResult(freelancer, relevanceScore));
            }
        }

        // Sort by relevance score (highest first)
        Collections.sort(searchResults, (a, b) -> Integer.compare(b.relevanceScore, a.relevanceScore));

        // Extract the sorted freelancers
        for (FreelancerSearchResult result : searchResults) {
            filteredList.add(result.freelancer);
        }

        return filteredList;
    }

    // Helper class to store freelancer with relevance score
    private static class FreelancerSearchResult {
        Freelancer freelancer;
        int relevanceScore;

        FreelancerSearchResult(Freelancer freelancer, int relevanceScore) {
            this.freelancer = freelancer;
            this.relevanceScore = relevanceScore;
        }
    }
}

