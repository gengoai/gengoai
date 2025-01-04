package com.gengoai.apollo.model.embedding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VSQueryResult implements Comparable<VSQueryResult> {
   String id;
   double score;

   @Override
   public int compareTo(@NotNull VSQueryResult vsQueryResult) {
      return Double.compare(score, vsQueryResult.score);
   }
} // END OF VSQueryResult
