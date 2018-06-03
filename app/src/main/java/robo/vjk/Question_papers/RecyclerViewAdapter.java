package robo.vjk.Question_papers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;



public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>
        implements Filterable {
    private Context context;
    private List<PDF> pdfList;
    private List<PDF> pdfListFiltered;
    private PDFAdapterListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, url;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.name);
            url = view.findViewById(R.id.phone);
            thumbnail = view.findViewById(R.id.thumbnail);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onPDFSelected(pdfListFiltered.get(getAdapterPosition()));
                }
            });
        }
    }


    public RecyclerViewAdapter(Context context, List<PDF> pdfList, PDFAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.pdfList = pdfList;
        this.pdfListFiltered = pdfList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final PDF pdf = pdfListFiltered.get(position);
        holder.title.setText(pdf.getTitle());
        //holder.url.setText(pdf.getUrl());

       // Glide.with(context).load(pdf.getImage()).apply(RequestOptions.circleCropTransform()).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return pdfListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    pdfListFiltered = pdfList;
                } else {
                    List<PDF> filteredList = new ArrayList<>();
                    for (PDF row : pdfList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase()) ) {  // || row.getPhone().contains(charSequence)
                            filteredList.add(row);
                        }
                    }

                    pdfListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = pdfListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                pdfListFiltered = (ArrayList<PDF>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface PDFAdapterListener {
        void onPDFSelected(PDF contact);
    }
}
