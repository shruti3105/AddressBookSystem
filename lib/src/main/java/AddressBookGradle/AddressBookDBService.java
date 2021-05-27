package AddressBookGradle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddressBookDBService {

	private PreparedStatement addressBookPreparedStatement;
	private static AddressBookDBService addressBookDBService;
	private List<AddressBookDetails> addressBookDetails;

	private AddressBookDBService() {
	}

	private Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/addressBook?useSSL=false";
		String username = "root";
		String password = "Shruti@3105";
		Connection con;
		System.out.println("Connecting to database:" + jdbcURL);
		con = DriverManager.getConnection(jdbcURL, username, password);
		System.out.println("Connection is successful:" + con);
		return con;

	}

	public static AddressBookDBService getInstance() {
		if (addressBookDBService == null)
			addressBookDBService = new AddressBookDBService();
		return addressBookDBService;
	}

	public List<AddressBookDetails> readData() throws AddressBookException {
		String query = null;
		query = "select * from address_book";
		return getAddressBookDetailsUsingDB(query);
	}

	private List<AddressBookDetails> getAddressBookDetailsUsingDB(String sql) throws AddressBookException {
		List<AddressBookDetails> addressBookDetails = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			addressBookDetails = this.getAddressBookDetails(resultSet);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
		return addressBookDetails;
	}

	private void prepareAddressBookStatement() throws AddressBookException {
		try {
			Connection connection = this.getConnection();
			String query = "select * from address_book where FirstName = ?";
			addressBookPreparedStatement = connection.prepareStatement(query);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
	}

	private List<AddressBookDetails> getAddressBookDetails(ResultSet resultSet) throws AddressBookException {
		List<AddressBookDetails> addressBookDetails = new ArrayList<>();
		try {
			while (resultSet.next()) {
				String firstName = resultSet.getString("FirstName");
				String lastName = resultSet.getString("LastName");
				String address = resultSet.getString("Address");
				String city = resultSet.getString("City");
				String state = resultSet.getString("State");
				String zip = resultSet.getString("Zip");
				String phoneNo = resultSet.getString("PhoneNumber");
				String email = resultSet.getString("Email");
				addressBookData.add(new AddressBookDetails(firstName, lastName, address, city, state, phoneNo, email));
			}
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
		return addressBookDetails;
	}

	public int updateAddressBookDetails(String firstname, String address) throws AddressBookException {
		try (Connection connection = this.getConnection()) {
			String query = String.format("update address_book set Address = '%s' where FirstName = '%s';", address,
					firstname);
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			return preparedStatement.executeUpdate(query);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.CONNECTION_FAILED);
		}
	}

	public List<AddressBookDetails> getAddressBookDetails(String firstname) throws AddressBookException {
		if (this.addressBookPreparedStatement == null)
			this.prepareAddressBookStatement();
		try {
			addressBookPreparedStatement.setString(1, firstname);
			ResultSet resultSet = addressBookPreparedStatement.executeQuery();
			addressBookDetails = this.getAddressBookDetails(resultSet);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.CONNECTION_FAILED);
		}
		System.out.println(addressBookDetials);
		return addressBookDetails;
	}

	public List<AddressBookDetails> readData(LocalDate start, LocalDate end) throws AddressBookException {
		String query = null;
		if (start != null)
			query = String.format("select * from addressBook where Date between '%s' and '%s';", start, end);
		if (start == null)
			query = "select * from addressBook";
		List<AddressBookDetails> addressBookList = new ArrayList<>();
		try (Connection con = this.getConnection();) {
			Statement statement = con.createStatement();
			ResultSet rs = statement.executeQuery(query);
			addressBookList = this.getAddressBookDetails(rs);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
		return addressBookList;
	}

	public int readDataBasedOnCity(String total, String city) throws AddressBookException {
		int count = 0;
		String query = String.format("select %s(state) from address_book where city = '%s' group by city;", total, city);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			resultSet.next();
			count = resultSet.getInt(1);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
		return count;
	}
	public AddressBookDetails addNewContact(String firstName, String lastName, String start, String address, String city, String state,
			String zip, String phoneNo, String email) throws AddressBookException {
		int id = -1;
		AddressBookDetails addressBookDetails = null;
		String query = String.format(
				"insert into address_book(FirstName, LastName, Date, Address, City, State, Zip, PhoneNo, Email) values ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s','%s')",
				firstName, lastName, start, address, city, state, zip, phoneNo, email);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			int rowChanged = statement.executeUpdate(query, statement.RETURN_GENERATED_KEYS);
			if (rowChanged == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					id = resultSet.getInt(1);
			}
			addressBookDetails = new AddressBookDetails(firstName, lastName, start, address, city, state, zip);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
		return addressBookDetails;
	}


}