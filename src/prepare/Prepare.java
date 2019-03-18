import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class Prepare {
    private static final String pathTxt = "src/prepare/txt/";
    private static final String pathSql = "src/prepare/sql/";

    private static Map<String, Integer> genres = new HashMap<>();
    private static Map<String, Integer> users = new HashMap<>();
    private static Map<String, Integer> movies = new HashMap<>();
    private static Map<String, String> posters = new HashMap<>();
    private static Map<String, Integer> countries = new HashMap<>();

    public static void main(String[] args) throws IOException {
        String line;
        int id;


        // Genre
        BufferedReader reader = new BufferedReader(new FileReader(pathTxt + "genre.txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter(pathSql + "genre.sql"));

        id = 1;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                genres.put(line, id);
                writer.write("insert into movieland.genre(genre_id, name) values (" + id + ", '" + line + "');");
                writer.newLine();
                id++;
            }
        }
        reader.close();
        writer.close();

        // User
        reader = new BufferedReader(new FileReader(pathTxt + "user.txt"));
        writer = new BufferedWriter(new FileWriter(pathSql + "user.sql"));

        id = 1;
        String userName = "";
        String userEmail = "";
        String userPassword = "";
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }

            if (userName.isEmpty()) {
                userName = line;
            } else if (userEmail.isEmpty()) {
                userEmail = line;
            } else if (userPassword.isEmpty()) {
                userPassword = line;
            }

            if (!userPassword.isEmpty()) {
                String salt = getSalt();
                String hash = getHash(userPassword + salt);
                String role = "USER";

                users.put(userName, id);

                writer.write("insert into movieland.user(user_id, name, email, role, hash, salt) values (" + id + ", '" + userName + "', '" + userEmail + "', '" + role + "', '" + hash + "', '" + salt + "');");
                writer.newLine();
                userName = "";
                userEmail = "";
                userPassword = "";
                id++;
            }
        }
        reader.close();
        writer.close();

        // Poster
        reader = new BufferedReader(new FileReader(pathTxt + "poster.txt"));

        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                String name = line.split(" https:")[0];
                String url = "https:" + line.split(" https:")[1];
                posters.put(name, url);
            }
        }
        reader.close();

        // Movie
        reader = new BufferedReader(new FileReader(pathTxt + "movie.txt"));
        writer = new BufferedWriter(new FileWriter(pathSql + "movie.sql"));
        BufferedWriter writerMG = new BufferedWriter(new FileWriter(pathSql + "movie_genre.sql"));
        BufferedWriter writerMC = new BufferedWriter(new FileWriter(pathSql + "movie_country.sql"));

        id = 1;
        int countryId = 1;
        String mName = "";
        String mYear = "";
        String mCountry = "";
        String mGenre = "";
        String mDescription = "";
        String mRating = "";
        String mPrice = "";
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }

            if (mName.isEmpty()) {
                mName = line.replaceAll("'", "''");
            } else if (mYear.isEmpty()) {
                mYear = line;
            } else if (mCountry.isEmpty()) {
                mCountry = line.replaceAll("'", "''");
            } else if (mGenre.isEmpty()) {
                mGenre = line;
            } else if (mDescription.isEmpty()) {
                mDescription = line.replaceAll("'", "''");
            } else if (mRating.isEmpty()) {
                mRating = line;
            } else if (mPrice.isEmpty()) {
                mPrice = line;
            }

            if (!mPrice.isEmpty()) {
                String rating = mRating.split(":")[1];
                String price = mPrice.split(":")[1];
                String nameRus = mName.split("/")[0];
                String nameOrig = mName.split("/")[1];
                String posterUrl = posters.get(nameRus);

                if (posterUrl == null) {
                    throw new NoSuchElementException("No poster for movie " + nameRus);
                }

                movies.put(nameRus, id);

                writer.write("insert into movieland.movie(movie_id, name, name_original, year, description, poster_url, rating, price) values (" +
                        id + ", '" + nameRus + "', '" + nameOrig + "', '" + mYear + "', '" + mDescription + "', '" + posterUrl + "', " + rating + ", " + price + ");");
                writer.newLine();

                String[] genreList = mGenre.split(", ");
                for (String genre : genreList) {
                    writerMG.write("insert into movieland.movie_genre(movie_id, genre_id) values (" + id + ", " + genres.get(genre) + ");");
                    writerMG.newLine();
                }

                String[] countryList = mCountry.split(", ");
                for (String country : countryList) {
                    if (!countries.containsKey(country)) {
                        countries.put(country, countryId);
                        countryId++;
                    }
                    writerMC.write("insert into movieland.movie_country(movie_id, country_id) values (" + id + ", " + countries.get(country) + ");");
                    writerMC.newLine();
                }


                mName = "";
                mYear = "";
                mCountry = "";
                mGenre = "";
                mDescription = "";
                mRating = "";
                mPrice = "";
                id++;
            }
        }
        reader.close();
        writer.close();
        writerMG.close();
        writerMC.close();

        // Countries
        writer = new BufferedWriter(new FileWriter(pathSql + "country.sql"));
        for (String country : countries.keySet()) {
            writer.write("insert into movieland.country(country_id, name) values (" + countries.get(country) + ", '" + country + "');");
            writer.newLine();
        }
        writer.close();


        // Review
        reader = new BufferedReader(new FileReader(pathTxt + "review.txt"));
        writer = new BufferedWriter(new FileWriter(pathSql + "review.sql"));

        id = 1;
        String rMovie = "";
        String rUser = "";
        String rText = "";
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }

            if (rMovie.isEmpty()) {
                rMovie = line;
            } else if (rUser.isEmpty()) {
                rUser = line;
            } else if (rText.isEmpty()) {
                rText = line;
            }

            if (!rText.isEmpty()) {
                int movieId = movies.get(rMovie);
                int userId = users.get(rUser);

                writer.write("insert into movieland.review(review_id, movie_id, user_id, text) values (" + id + ", " + movieId + ", " + userId + ", '" + rText + "');");
                writer.newLine();
                rMovie = "";
                rUser = "";
                rText = "";
                id++;
            }
        }
        reader.close();
        writer.close();
    }

    private static String getHash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getSalt() {
        Random RANDOM = new SecureRandom();
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
