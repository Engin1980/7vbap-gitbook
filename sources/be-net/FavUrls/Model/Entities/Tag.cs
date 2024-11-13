using System.ComponentModel.DataAnnotations.Schema;

namespace FavUrls.Model.Entities
{
  public class Tag
  {
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int TagId { get; set; }
    public string Title { get; set; } = null!;
    public string Color { get; set; } = null!;
    public List<Url> Urls { get; set; } = [];
    public AppUser AppUser { get; set; } = null!;
  }
}
