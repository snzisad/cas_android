package com.client.snzisad.casmobile.BusinessClass;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.client.snzisad.casmobile.AuctionDetailsActivity;
import com.client.snzisad.casmobile.ProductDetailsActivity;
import com.client.snzisad.casmobile.R;

import java.util.HashMap;
import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolderClass>{
    private Context context;
    private List<HashMap<String, String>> productlist;

    public ProductListAdapter(Context context, List<HashMap<String, String>> productlist){
        this.context = context;
        this.productlist = productlist;
    }

    @NonNull
    @Override
    public ViewHolderClass onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_product_list, viewGroup,false);
        ViewHolderClass viewHolderClass = new ViewHolderClass(v);
        return viewHolderClass;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderClass viewHolderClass, int i) {
        final HashMap<String, String> product = productlist.get(i);
        String imageURL = APILink.host+product.get("image");

        if(DataHandler.postOrAuction == 0){
            viewHolderClass.tvProductName.setText(product.get("post_title"));
            viewHolderClass.tvProductPrice.setText("Tk. "+product.get("price"));
            viewHolderClass.tvProductLocation.setText(product.get("location"));
        }
        else{
            viewHolderClass.tvProductName.setText(product.get("auction_title"));
            viewHolderClass.tvProductPrice.setText("Tk. "+product.get("price"));
            viewHolderClass.tvProductLocation.setText("Ending Date: "+product.get("ending_date"));
        }

        Glide.with(context).load(imageURL).into(viewHolderClass.imgProductThumb);

        viewHolderClass.cardProductInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataHandler.productID = Integer.parseInt(product.get("id"));

                if(DataHandler.postOrAuction == 0) {
                    context.startActivity(new Intent(context, ProductDetailsActivity.class));
                }
                else{
                    context.startActivity(new Intent(context, AuctionDetailsActivity.class));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return productlist.size();
    }

    public class ViewHolderClass extends RecyclerView.ViewHolder{

        TextView tvProductName, tvProductPrice, tvProductLocation;
        ImageView imgProductThumb;
        CardView cardProductInfo;

        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);

            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductLocation = itemView.findViewById(R.id.tvProductLocation);
            imgProductThumb = itemView.findViewById(R.id.imgProductThumb);
            cardProductInfo = itemView.findViewById(R.id.cardProductInfo);
        }
    }
}
